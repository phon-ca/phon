/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.session.io.xml.v12;

import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;

import javax.xml.bind.*;
import javax.xml.datatype.*;
import javax.xml.datatype.Duration;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import org.apache.commons.lang3.*;
import org.apache.logging.log4j.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.orthography.*;
import ca.phon.plugin.*;
import ca.phon.session.*;
import ca.phon.session.Comment;
import ca.phon.session.Record;
import ca.phon.session.io.*;
import ca.phon.syllable.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;
import ca.phon.xml.*;
import ca.phon.xml.annotation.*;

/**
 * Session XML reader for session files with
 * version 'PB1.2'
 *
 */
@XMLSerial(
	namespace="http://phon.ling.mun.ca/ns/phonbank",
	elementName="session",
	bindType=Session.class
)
@SessionIO(
		group="ca.phon",
		id="phonbank",
		version="1.2",
		mimetype="application/xml",
		extension="xml",
		name="Phon (.xml)"
)
@Rank(0)
public class XMLSessionReader_v12 implements SessionReader, XMLObjectReader<Session>, IPluginExtensionPoint<SessionReader> {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(XMLSessionReader_v12.class.getName());

	@Override
	public Session read(Document doc, Element ele)
			throws IOException {
		Session retVal = null;

		final ObjectFactory xmlFactory = new ObjectFactory();

		try {
			final JAXBContext context = JAXBContext.newInstance(xmlFactory.getClass());
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			final JAXBElement<SessionType> sessionTypeEle =
					unmarshaller.unmarshal(doc.getDocumentElement(), SessionType.class);

			if(sessionTypeEle != null && sessionTypeEle.getValue() != null) {
				final SessionType sessionType = sessionTypeEle.getValue();
				retVal = readSessionType(sessionType);
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		}

		return retVal;
	}

	/**
	 * Read session in from given xml {@link SessionType} object.
	 *
	 * @param sessionType
	 *
	 * @return session
	 */
	private Session readSessionType(SessionType sessionType) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Session retVal = factory.createSession();

		// copy header info
		retVal.setName(sessionType.getId());
		retVal.setCorpus(sessionType.getCorpus());

		final HeaderType headerData = sessionType.getHeader();
		if(headerData != null) {
			if(headerData.getMedia() != null && headerData.getMedia().length() > 0) {
				retVal.setMediaLocation(headerData.getMedia());
			}
			if(headerData.getDate() != null) {
				final XMLGregorianCalendar xmlDate = headerData.getDate();
				final LocalDate dateTime = LocalDate.of(
						xmlDate.getYear(),
						xmlDate.getMonth(),
						xmlDate.getDay());
				retVal.setDate(dateTime);
			}
			if(headerData.getLanguage().size() > 0) {
				String langs = "";
				for(String lang:headerData.getLanguage()) {
					langs += (langs.length() > 0 ? " " : "") + lang;
				}
				retVal.setLanguage(langs);
			}
		}

		// copy participant information
		final ParticipantsType participants = sessionType.getParticipants();
		if(participants != null) {
			for(ParticipantType pt:participants.getParticipant()) {
				final Participant p = copyParticipant(factory, pt, retVal.getDate());
				retVal.addParticipant(p);
			}
		}

		// copy transcriber information
		final TranscribersType transcribers = sessionType.getTranscribers();
		if(transcribers != null) {
			for(TranscriberType tt:transcribers.getTranscriber()) {
				final Transcriber t = copyTranscriber(factory, tt);
				retVal.addTranscriber(t);
			}
		}

		// copy tier information
		final UserTiersType userTiers = sessionType.getUserTiers();
		if(userTiers != null) {
			for(UserTierType utt:userTiers.getUserTier()) {
				final TierDescription td = copyTierDescription(factory, utt);
				retVal.addUserTier(td);
			}
		}

		final List<TierViewItem> tierOrder = new ArrayList<TierViewItem>();
		for(TvType tot:sessionType.getTierOrder().getTier()) {
			final TierViewItem toi = copyTierViewItem(factory, tot);
			tierOrder.add(toi);
		}
		retVal.setTierView(tierOrder);

		// copy transcript data
		final List<Comment> recordComments = new ArrayList<Comment>();
		boolean foundFirstRecord = false;
		if(sessionType.getTranscript() != null) {
			for(Object uOrComment:sessionType.getTranscript().getUOrComment()) {
				if(uOrComment instanceof CommentType) {
					final CommentType ct = (CommentType)uOrComment;
					final Comment comment = copyComment(factory, ct);
					recordComments.add(comment);
				} else {
					if(!foundFirstRecord && recordComments.size() > 0) {
						// add record comments to session metadata
						for(Comment c:recordComments) {
							retVal.getMetadata().addComment(c);
						}
						recordComments.clear();
					}
					final RecordType rt = (RecordType)uOrComment;


					Record record = null;
					try {
						record = factory.createRecord(new LazyRecord(factory, retVal, rt));
					} catch (Exception e) {
						LOGGER.info(rt.getId());
						LOGGER.error(
								e.getLocalizedMessage(), e);

					}

					retVal.addRecord(record);

					for(Comment comment:recordComments) {
						record.addComment(comment);
					}
					recordComments.clear();
					foundFirstRecord = true;
				}
			}
			if(!foundFirstRecord && recordComments.size() > 0) {
				// add record comments to session metadata
				for(Comment c:recordComments) {
					retVal.getMetadata().addComment(c);
				}
			}
		}

		return retVal;
	}

	// participants
	Participant copyParticipant(SessionFactory factory, ParticipantType pt, LocalDate sessionDate) {
		final Participant retVal = factory.createParticipant();

		retVal.setId(pt.getId());
		retVal.setName(pt.getName());

		final XMLGregorianCalendar bday = pt.getBirthday();
		if(bday != null) {
			final LocalDate bdt = LocalDate.of(bday.getYear(), bday.getMonth(), bday.getDay());
			retVal.setBirthDate(bdt);

			// calculate age up to the session date
			if(sessionDate != null) {
				final Period period = Period.between(bdt, sessionDate);
				retVal.setAgeTo(period);
			}
		}

		final Duration ageDuration = pt.getAge();
		if(ageDuration != null) {
			// convert to period
			final Period age = Period.of(ageDuration.getYears(), ageDuration.getMonths(), ageDuration.getDays());
			retVal.setAge(age);
		}

		retVal.setEducation(pt.getEducation());
		retVal.setGroup(pt.getGroup());

		String langs = "";
		for(String lang:pt.getLanguage())
			langs += (langs.length() > 0 ? " " : "") + lang;
		retVal.setLanguage(langs);

		if(pt.getSex() == SexType.MALE)
			retVal.setSex(Sex.MALE);
		else if(pt.getSex() == SexType.FEMALE)
			retVal.setSex(Sex.FEMALE);
		else
			retVal.setSex(Sex.UNSPECIFIED);

		ParticipantRole prole = ParticipantRole.fromString(pt.getRole());
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole);

		retVal.setSES(pt.getSES());

		return retVal;
	}

	// transcribers
	private Transcriber copyTranscriber(SessionFactory factory, TranscriberType tt) {
		final Transcriber retVal = factory.createTranscriber();

		retVal.setUsername(tt.getId());
		retVal.setRealName(tt.getName());

		if(tt.getPassword() != null) {
			retVal.setPassword(tt.getPassword().getContent());
			retVal.setUsePassword(tt.getPassword().isUse());
		}

		return retVal;
	}

	// tier descriptions
	private TierDescription copyTierDescription(SessionFactory factory, UserTierType utt) {
		final boolean grouped = utt.isGrouped();
		final String name = utt.tierName;
		
		try {
			Class<?> type = Class.forName(utt.getType(), true, PluginManager.getInstance());
			return factory.createTierDescription(name, grouped, type);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private TierViewItem copyTierViewItem(SessionFactory factory, TvType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();

		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	private MediaSegment copySegment(SessionFactory factory, SegmentType st) {
		final MediaSegment segment = factory.createMediaSegment();
		segment.setStartValue(Math.abs(st.getStartTime()));
		segment.setEndValue(Math.abs(st.getStartTime()) + Math.abs(st.getDuration()));
		segment.setUnitType(MediaUnit.Millisecond);
		return segment;
	}

	// copy comment data
	private Comment copyComment(SessionFactory factory, CommentType ct) {
		final String tag = ct.getType();
		final StringBuffer buffer = new StringBuffer();
		MediaSegment segment = null;
		for(Object obj:ct.getContent()) {
			if(obj instanceof JAXBElement) {
				final JAXBElement<?> jaxbEle = (JAXBElement<?>)obj;
				if(jaxbEle.getDeclaredType() == SegmentType.class)
					segment = copySegment(factory, (SegmentType)jaxbEle.getValue());
			} else {
				buffer.append(obj.toString());
			}
		}
		return factory.createComment(tag, buffer.toString(), segment);
	}

	Record copyRecord(SessionFactory factory, Session session, RecordType rt) {
		final Record retVal = factory.createRecord();

		retVal.setExcludeFromSearches(rt.isExcludeFromSearches());

		try {
			if(rt.getId() != null) {
				UUID uuid = UUID.fromString(rt.getId());
				retVal.setUuid(uuid);
			}
		} catch (IllegalArgumentException e) {
		}

		if(rt.getSpeaker() != null) {
			final ParticipantType pt = (ParticipantType)rt.getSpeaker();
			for(int pIdx = 0; pIdx < session.getParticipantCount(); pIdx++) {
				final Participant participant = session.getParticipant(pIdx);
				if(participant.getName() != null  && participant.getName().equals(pt.getName())) {
					retVal.setSpeaker(participant);
					break;
				} else if(participant.getId() != null && participant.getId().equals(pt.getId())) {
					retVal.setSpeaker(participant);
					break;
				}
			}
		} else {
			retVal.setSpeaker(Participant.UNKNOWN);
		}

		// orthography
		final OrthographyType ot = rt.getOrthography();
		final Tier<Orthography> orthoTier = copyOrthography(factory, ot);
		retVal.setOrthography(orthoTier);

		// ipa target/actual
		for(IpaTierType ipaTt:rt.getIpaTier()) {
			final Tier<IPATranscript> ipaTranscript = copyTranscript(factory, ipaTt);
			if(ipaTt.getForm() == PhoTypeType.ACTUAL) {
				retVal.setIPAActual(ipaTranscript);
			} else {
				retVal.setIPATarget(ipaTranscript);
			}
		}

		Tier<IPATranscript> ipaTargetTier = retVal.getIPATarget();
		while(ipaTargetTier.numberOfGroups() < retVal.numberOfGroups()) ipaTargetTier.addGroup();

		Tier<IPATranscript> ipaActualTier = retVal.getIPAActual();
		while(ipaActualTier.numberOfGroups() < retVal.numberOfGroups()) ipaActualTier.addGroup();

		// blind transcriptions
		for(BlindTierType btt:rt.getBlindTranscription()) {
			// get the correct ipa object from our new record
			final Tier<IPATranscript> ipaTier =
					(btt.getForm() == PhoTypeType.MODEL ? retVal.getIPATarget() : retVal.getIPAActual());
			int gidx = 0;
			for(BgType bgt:btt.getBg()) {
				final StringBuffer buffer = new StringBuffer();
				for(WordType wt:bgt.getW()) {
					if(buffer.length() > 0)
						buffer.append(" ");
					buffer.append(wt.getContent());
				}

				final IPATranscript ipa = (gidx < ipaTier.numberOfGroups() ? ipaTier.getGroup(gidx) : null);

				if(ipa != null) {
					try {
						final IPATranscript blindTranscript =
								IPATranscript.parseIPATranscript(buffer.toString());
						final TranscriberType tt = (TranscriberType)btt.getUser();
						final String name = tt.getId();

						AlternativeTranscript at = ipa.getExtension(AlternativeTranscript.class);
						if(at == null) {
							at = new AlternativeTranscript();
							ipa.putExtension(AlternativeTranscript.class, at);
						}
						at.put(name, blindTranscript);
					} catch (ParseException e) {
						LOGGER.info(
								e.getLocalizedMessage(), e);
					}
				}
				gidx++;
			}
		}

		// notes
		if(rt.getNotes() != null)
			retVal.getNotes().setGroup(0, new TierString(rt.getNotes().getContent()));

		// segment
		if(rt.getSegment() != null) {
			final MediaSegment segment = copySegment(factory, rt.getSegment());
			retVal.getSegment().setGroup(0, segment);
		} else {
			retVal.getSegment().setGroup(0, factory.createMediaSegment());
		}

		// alignment
		for(AlignmentTierType att:rt.getAlignment()) {
			final Tier<PhoneMap> alignment = copyAlignment(factory, retVal, att);
			while(alignment.numberOfGroups() < retVal.numberOfGroups()) alignment.addGroup();
			retVal.setPhoneAlignment(alignment);
			break; // only processing the first alignment element (which should be the only one)
		}

		// user tiers
		for(FlatTierType ftt:rt.getFlatTier()) {
			final Tier<TierString> flatTier = factory.createTier(ftt.getTierName(), TierString.class, false);
			flatTier.setGroup(0, new TierString(ftt.getContent()));
			retVal.putTier(flatTier);
		}

		for(GroupTierType gtt:rt.getGroupTier()) {
			final Tier<TierString> groupTier = factory.createTier(gtt.getTierName(), TierString.class, true);
			int gidx = 0;
			for(TgType tgt:gtt.getTg()) {
				final StringBuffer buffer = new StringBuffer();
				for(WordType wt:tgt.getW()) {
					if(buffer.length() > 0)
						buffer.append(" ");
					buffer.append(wt.getContent());
				}
				groupTier.setGroup(gidx++, new TierString(buffer.toString()));
			}
			// ensure the dependent tier has the correct number of groups
			while(groupTier.numberOfGroups() < retVal.numberOfGroups()) {
				groupTier.addGroup();
			}
			retVal.putTier(groupTier);
		}

		return retVal;
	}

	/**
	 * Copy orthography
	 *
	 * @param factory
	 * @param ot
	 * @return
	 */
	private Tier<Orthography> copyOrthography(SessionFactory factory, OrthographyType ot) {
		// first create a string from the orthography type,
		// then parse the string into the new orthography container

		final Tier<Orthography> retVal = factory.createTier(SystemTierType.Orthography.getName(), Orthography.class, SystemTierType.Orthography.isGrouped());

		for(Object otEle:ot.getWOrGOrP()) {
			if(!(otEle instanceof GroupType)) continue;

			final GroupType gt = (GroupType)otEle;
			final List<String> eleList = new ArrayList<String>();
			for(Object ele:gt.getWOrComOrE()) {
				if(ele instanceof WordType) {
					WordType w = (WordType)ele;

					String lastV = null;
					if(eleList.size() > 0) {
						lastV = eleList.get(eleList.size()-1);
					}

					String wTxt = w.getContent();
					if(
							/* compound marker */ wTxt.equals("+") ||
							/* seond part of cmp */ (lastV != null && lastV.endsWith("+")) ) {
						// add to previous word data
						if(lastV != null) {
							eleList.remove(lastV);
							lastV += wTxt;
							eleList.add(lastV);
						} else {
							eleList.add(wTxt);
						}
					} else {
						eleList.add(w.getContent());
					}
				} else if(ele instanceof EventType) {
					EventType e = (EventType)ele;
					eleList.add("*" + e.getContent() + "*");
				} else if(ele instanceof CommentType) {
					CommentType oct = (CommentType)ele;
					final StringBuffer buffer = new StringBuffer();
					for(Object obj:oct.getContent()) buffer.append(obj);
					eleList.add("(" + (oct.getType() != null ? oct.getType() + ":" : "") + buffer.toString() + ")");
				} else if(ele instanceof InnerGroupMarker) {
					InnerGroupMarker ig = (InnerGroupMarker)ele;
					if(ig.getType() == InnerGroupMarkerType.S)
						eleList.add("{");
					else
						eleList.add("}");
				} else if(ele instanceof PunctuationType) {
					PunctuationType pt = (PunctuationType)ele;
					eleList.add(pt.getContent());
				}
			}

			final StringBuffer buffer = new StringBuffer();
			for(String ele:eleList) {
				if(buffer.length() > 0)
					buffer.append(" ");
				buffer.append(ele);
			}

			final String orthoTxt = buffer.toString();
			Orthography ortho = new Orthography();
			try {
				ortho = Orthography.parseOrthography(orthoTxt);
			} catch (ParseException pe) {
				final UnvalidatedValue uv = new UnvalidatedValue(orthoTxt, pe);
				ortho.putExtension(UnvalidatedValue.class, uv);
			}
			retVal.addGroup(ortho);
		}

		return retVal;
	}

	/**
	 * Copy ipa data.
	 *
	 * Attempts to copy phone objects as-is, if not possible
	 * the transcription is re-parsed.
	 *
	 * @param factory
	 * @param ipaType
	 */
	private Tier<IPATranscript> copyTranscript(SessionFactory factory, IpaTierType itt) {
		final SystemTierType tierType =
				(itt.getForm() == PhoTypeType.MODEL ? SystemTierType.IPATarget : SystemTierType.IPAActual);
		final Tier<IPATranscript> retVal = factory.createTier(tierType.getName(), IPATranscript.class, tierType.isGrouped());

		// attempt an exact copy first
		for(PhoType pt:itt.getPg()) {

			if(pt != null && pt.getW() != null) {
				final StringBuffer groupBuffer = new StringBuffer();
				for(WordType wt:pt.getW()) {
					if(groupBuffer.length() > 0)
						groupBuffer.append(" ");
					groupBuffer.append(wt.getContent());
				}

				if(groupBuffer.toString().trim().length() == 0) {
					retVal.addGroup(new IPATranscript());
				} else {
					try {
						final IPATranscript transcript = IPATranscript.parseIPATranscript(groupBuffer.toString());
						// copy syllabification if transcript is the same size as our provided syllabification
						if(pt.getSb() != null && pt.getSb().getPh() != null
								&& transcript.length() == pt.getSb().getPh().size()) {
							final CopyTranscriptVisitor visitor = new CopyTranscriptVisitor(pt.getSb().getPh());
							transcript.accept(visitor);
						}
						retVal.addGroup(transcript);
					} catch (ParseException pe) {
						LOGGER.info( pe.getLocalizedMessage(), pe);

						final IPATranscript ipa = new IPATranscript();
						ipa.putExtension(UnvalidatedValue.class, new UnvalidatedValue(groupBuffer.toString(), pe));
						retVal.addGroup(ipa);
					}
				}
			} else {
				retVal.addGroup(new IPATranscript());
			}
		}

		return retVal;
	}

	public class CopyTranscriptVisitor extends VisitorAdapter<IPAElement> {

		int eleIdx = 0;

		final List<ConstituentType> syllabification;

		public CopyTranscriptVisitor(List<ConstituentType> syllabification) {
			super();
			this.syllabification = syllabification;
		}

		@Override
		public void fallbackVisit(IPAElement obj) {
			eleIdx++;
		}

		@Visits
		public void visitPhone(Phone phone) {
			final ConstituentType ct =
					(eleIdx < syllabification.size() ? syllabification.get(eleIdx++) : null);
			if(ct != null) {
				final ConstituentTypeType ctt = ct.getScType();
				final SyllableConstituentType scType = SyllableConstituentType.fromString(ctt.toString());
				final SyllabificationInfo info = phone.getExtension(SyllabificationInfo.class);
				if(scType != null) {
					info.setConstituentType(scType);
					if(scType == SyllableConstituentType.NUCLEUS) {
						info.setDiphthongMember(!ct.isHiatus());
					}
					phone.putExtension(SyllabificationInfo.class, info);
				}
			}
		}

		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			final ConstituentType ct =
					(eleIdx < syllabification.size() ? syllabification.get(eleIdx++) : null);
			if(ct != null) {
				final ConstituentTypeType ctt = ct.getScType();
				final SyllabificationInfo info = cp.getExtension(SyllabificationInfo.class);
				final SyllableConstituentType scType = SyllableConstituentType.fromString(ctt.toString());
				if(scType != null) {
					info.setConstituentType(scType);
					if(scType == SyllableConstituentType.NUCLEUS) {
						info.setDiphthongMember(!ct.isHiatus());
					}
				}
			}
		}

	}

	/**
	 * Copy alignment data
	 */
	private Tier<PhoneMap> copyAlignment(SessionFactory factory, Record record, AlignmentTierType att) {
		final Tier<PhoneMap> retVal = factory.createTier(SystemTierType.SyllableAlignment.getName(), PhoneMap.class, true);

		// create 'sound-only' lists from the ipa transcripts.  These are used for alignment
		// indices
		final Tier<IPATranscript> ipaT = record.getIPATarget();
		final Tier<IPATranscript> ipaA = record.getIPAActual();

		int gidx = 0;
		for(AlignmentType at:att.getAg()) {
			final IPATranscript ipaTGrp = (ipaT.numberOfGroups() > gidx && ipaT.getGroup(gidx) != null ?
					ipaT.getGroup(gidx) : new IPATranscript());
			final IPATranscript ipaAGrp = (ipaA.numberOfGroups() > gidx && ipaA.getGroup(gidx) != null ?
					ipaA.getGroup(gidx) : new IPATranscript());

			final PhoneMap pm = new PhoneMap(ipaTGrp, ipaAGrp);

			final Integer[][] alignmentData = new Integer[2][];
			alignmentData[0] = new Integer[at.getLength()];
			alignmentData[1] = new Integer[at.getLength()];

			for(int i = 0; i < at.getPhomap().size(); i++) {
				final MappingType mt = at.getPhomap().get(i);
				alignmentData[0][i] =
						(mt.getValue().size() > 0 ? mt.getValue().get(0) : null);
				alignmentData[1][i] =
						(mt.getValue().size() > 1 ? mt.getValue().get(1) : null);
			}
			pm.setTopAlignment(alignmentData[0]);
			pm.setBottomAlignment(alignmentData[1]);
//
			retVal.addGroup(pm);
			gidx++;
		}

		return retVal;
	}

	/**
	 * Get an dom version of the xml stream
	 *
	 * @param in
	 * @return dom document
	 */
	private Document documentFromStream(InputStream stream)
		throws IOException {
		Document retVal = null;

		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			retVal = builder.parse(stream);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}

		return retVal;
	}

	@Override
	public Session readSession(InputStream stream) throws IOException {
		final Document doc = documentFromStream(stream);
		return read(doc, doc.getDocumentElement());
	}

	@Override
	public boolean canRead(File file) throws IOException {
		// open file and make sure the first
		// element is 'session' with the correct version
		boolean canRead = false;

		// use StAX to read only first element
		// create StAX reader
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLEventReader reader = null;
		try(FileInputStream source = new FileInputStream(file)) {
			//BufferedReader in = new BufferedReader(new InputStreamReader(source, "UTF-8"));
			XMLEventReader xmlReader = factory.createXMLEventReader(source, "UTF-8");
			reader = factory.createFilteredReader(xmlReader, new XMLWhitespaceFilter());

			XMLEvent evt;
			while(!(evt = reader.nextEvent()).isStartElement());
			canRead =
					evt.asStartElement().getName().getLocalPart().equals("session")
					&& evt.asStartElement().getAttributeByName(new QName("version")).getValue().equals("PB1.2");
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}

		return canRead;
	}

	private class XMLWhitespaceFilter implements EventFilter {

		@Override
		public boolean accept(XMLEvent arg0) {
			boolean retVal = true;


			if(arg0.isCharacters() &&
					StringUtils.strip(arg0.asCharacters().getData()).length() == 0) {

				retVal = false;
			}

			return retVal;
		}

	}

	@Override
	public Class<?> getExtensionType() {
		return SessionReader.class;
	}

	@Override
	public IPluginExtensionFactory<SessionReader> getFactory() {
		return (args) -> { return new XMLSessionReader_v12(); };
	}

}

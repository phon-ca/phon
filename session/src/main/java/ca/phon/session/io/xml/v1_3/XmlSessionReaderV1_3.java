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
package ca.phon.session.io.xml.v1_3;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.*;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.UserTierData;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionReader;
import ca.phon.session.GroupSegment;
import ca.phon.session.io.xml.v13.*;
import ca.phon.session.io.xml.v13.CommentType;
import ca.phon.session.io.xml.v13.TierAlignmentRules;
import ca.phon.session.io.xml.v13.WordType;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Language;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import ca.phon.xml.XMLObjectReader;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * Session XML reader for session files with
 * version '1.3'
 *
 */
@XMLSerial(
	namespace="https://phon.ca/ns/phonbank",
	elementName="session",
	bindType=Session.class
)
@SessionIO(
		group="ca.phon",
		id="phonbank",
		version="1.3",
		mimetype="application/xml",
		extension="xml",
		name="Phon 4.0+ (.xml)"
)
@Rank(0)
public class XmlSessionReaderV1_3 implements SessionReader, XMLObjectReader<Session>, IPluginExtensionPoint<SessionReader> {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(XmlSessionReaderV1_3.class.getName());

	@Override
	public Session read(Document doc, Element ele)
			throws IOException {
		Session retVal = null;

		// ensure our element namespace is correct, required when reading PB1.2 documents
		ele.setAttribute("xmlns", "https://phon.ca/ns/phonbank");

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
			if(headerData.getLanguages().size() > 0) {
				List<Language> langs = new ArrayList<>();
				for(String lang:headerData.getLanguages()) {
					try {
						Language l = Language.parseLanguage(lang);
						langs.add(l);
					} catch (IllegalArgumentException e) {
						LOGGER.warn(e);
					}
				}
				retVal.setLanguages(langs);
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
			for(TierDescriptionType tdt:userTiers.getTd()) {
				final TierDescription td = readTierDescription(factory, tdt);
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
			for(Object uOrComment:sessionType.getTranscript().getROrComment()) {
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
			if(recordComments.size() > 0) {
				// add record comments to session metadata
				for(Comment c:recordComments) {
					retVal.getMetadata().addTrailingComment(c);
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

		if(pt.getBirthplace() != null) {
			retVal.setBirthplace(pt.getBirthplace());
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
		for(String lang:pt.getLanguages())
			langs += (langs.length() > 0 ? " " : "") + lang;
		retVal.setLanguage(langs);

		if(pt.getFirstLanguage() != null) {
			retVal.setFirstLanguage(pt.getFirstLanguage());
		}

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

		if(pt.getOther() != null) {
			retVal.setOther(pt.getOther());
		}

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
	private TierDescription readTierDescription(SessionFactory factory, TierDescriptionType tdt) {
		final String tierName = tdt.getTierName();
		final Map<String, String> tierParams = new LinkedHashMap<>();
		if(tdt.getTierParameters() != null) {
			for (TierParameterType tp : tdt.getTierParameters().getParam()) {
				tierParams.put(tp.getName(), tp.getContent());
			}
		}
		if(tdt.getTierAlignment() != null) {
			final TierAlignmentRules alignmentRules = tdt.getTierAlignment();

			final List<TypeAlignmentRules.AlignableType> alignableTypes = new ArrayList<>();

		}
	}

	private TierViewItem copyTierViewItem(SessionFactory factory, TierViewType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();

		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	private MediaSegment copySegment(SessionFactory factory, MediaType st) {
		final MediaSegment segment = factory.createMediaSegment();
		final float startVal = st.getStart().floatValue();
		final float endVal = st.getEnd().floatValue();
		segment.setStartValue(startVal);
		segment.setEndValue(endVal);
		final MediaUnit unit = switch(st.getUnit()) {
			case S -> MediaUnit.Second;
			case MS -> MediaUnit.Millisecond;
		};
		segment.setUnitType(unit);
		return segment;
	}

	// copy comment data
	private Comment copyComment(SessionFactory factory, CommentType ct) {
		final ca.phon.session.CommentType type = switch (ct.getType()) {
			case ACTIVITIES -> ca.phon.session.CommentType.Activities;
			case BCK -> ca.phon.session.CommentType.Bck;
			case BEGIN_GEM -> ca.phon.session.CommentType.Bg;
			case BLANK -> ca.phon.session.CommentType.Blank;
			case DATE -> ca.phon.session.CommentType.Date;
			case END_GEM -> ca.phon.session.CommentType.Eg;
			case GENERIC -> ca.phon.session.CommentType.Generic;
			case LAZY_GEM -> ca.phon.session.CommentType.G;
			case LOCATION -> ca.phon.session.CommentType.Location;
			case NEW_EPISODE -> ca.phon.session.CommentType.NewEpisode;
			case NUMBER -> ca.phon.session.CommentType.Number;
			case PAGE -> ca.phon.session.CommentType.Page;
			case RECORDING_QUALITY -> ca.phon.session.CommentType.RecordingQuality;
			case ROOM_LAYOUT -> ca.phon.session.CommentType.RoomLayout;
			case SITUATION -> ca.phon.session.CommentType.Situation;
			case T -> ca.phon.session.CommentType.T;
			case TAPE_LOCATION -> ca.phon.session.CommentType.TapeLocation;
			case TIME_DURATION -> ca.phon.session.CommentType.TimeDuration;
			case TIME_START -> ca.phon.session.CommentType.TimeStart;
			case TRANSCRIBER -> ca.phon.session.CommentType.Transcriber;
			case TRANSCRIPTION -> ca.phon.session.CommentType.Transcription;
			case TYPES -> ca.phon.session.CommentType.Types;
			case WARNING -> ca.phon.session.CommentType.Warning;
		};
		final List<UserTierElement> elements = new ArrayList<>();
		for(Object obj:ct.getTierData().getTwOrTcOrInternalMedia()) {
			if(!(obj instanceof JAXBElement<?> ele)) continue;
			if(ele.getName().getLocalPart().equals("tw")) {
				elements.add(new TierString(((JAXBElement<String>)ele).getValue()));
			} else if(ele.getName().getLocalPart().equals("tc")) {
				elements.add(new UserTierComment(((JAXBElement<String>)ele).getValue()));
			} else if(ele.getName().getLocalPart().equals("internal-media")) {
				final MediaType mediaType = ((JAXBElement<MediaType>)ele).getValue();
				float start = mediaType.getStart().floatValue();
				float end = mediaType.getEnd().floatValue();
				final MediaUnit unit = switch (mediaType.getUnit()) {
					case S -> MediaUnit.Second;
					case MS -> MediaUnit.Millisecond;
				};
				if(unit == MediaUnit.Millisecond) {
					start /= 1000.0f;
					end /= 1000.0f;
				}
				final InternalMedia imedia = new InternalMedia(start, end);
				elements.add(new UserTierInternalMedia(imedia));
			}
		}
		final UserTierData userTierData = new UserTierData(elements);
		return factory.createComment(type, userTierData);
	}

	Record copyRecord(SessionFactory factory, Session session, RecordType rt) {
		final Record retVal = factory.createRecord();

		retVal.setExcludeFromSearches(rt.isExcludeFromSearches());

		if(rt.getLanguage() != null) {
			try {
				Language l = Language.parseLanguage(rt.getLanguage());
				retVal.setLanguage(l);
			} catch (IllegalArgumentException e) {
			}
		}

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

		Tier<IPATranscript> ipaTargetTier = retVal.getIPATargetTier();
		while(ipaTargetTier.numberOfGroups() < retVal.numberOfGroups()) ipaTargetTier.addGroup();

		Tier<IPATranscript> ipaActualTier = retVal.getIPAActualTier();
		while(ipaActualTier.numberOfGroups() < retVal.numberOfGroups()) ipaActualTier.addGroup();

		// blind transcriptions
		for(BlindTierType btt:rt.getBlindTranscription()) {
			// get the correct ipa object from our new record
			final Tier<IPATranscript> ipaTier =
					(btt.getForm() == PhoTypeType.MODEL ? retVal.getIPATargetTier() : retVal.getIPAActualTier());
			int gidx = 0;
			for(BgType bgt:btt.getBg()) {
				final StringBuffer buffer = new StringBuffer();
				for(ca.phon.session.io.xml.v13.WordType wt:bgt.getW()) {
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
			retVal.getNotesTier().setGroup(0, new TierString(rt.getNotes().getContent()));

		// segment
		if(rt.getSegment() != null) {
			final MediaSegment segment = copySegment(factory, rt.getSegment());
			retVal.setMediaSegment(segment);

			for (ca.phon.session.io.xml.v13.GroupSegment gseg : rt.getSegment().getGseg()) {
				retVal.getGroupSegment().addGroup(new GroupSegment(retVal,
						gseg.getStart(), gseg.getEnd()));
			}
		} else {
			retVal.setMediaSegment(factory.createMediaSegment());
		}
		if(retVal.getGroupSegment().numberOfGroups() == 0) {
			// setup default group segment lengths as these are not stored prior to phonbank 1.3
			float start = 0.0f;
			float gwidth = (orthoTier.numberOfGroups() > 0 ? 1.0f / orthoTier.numberOfGroups() : 1.0f);
			for(int i = 0; i < orthoTier.numberOfGroups(); i++) {
				float end = Math.min(1.0f, start + gwidth);
				retVal.getGroupSegment().addGroup(new GroupSegment(retVal, start, end));
				start = end;
			}
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
				for(ca.phon.session.io.xml.v13.WordType wt:tgt.getW()) {
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
		final Tier<Orthography> retVal = factory.createTier(SystemTierType.Orthography.getName(), Orthography.class, SystemTierType.Orthography.isGrouped());

		for(Object uttGrp:ot.getUOrUnparsable()) {
			if(uttGrp instanceof XMLOrthographyUtteranceType) {
				final XMLOrthographyUtteranceType utt = (XMLOrthographyUtteranceType) uttGrp;
				final XmlOrthographyVisitor visitor = new XmlOrthographyVisitor();
				utt.getLinker().forEach(visitor::visit);
				utt.getWOrGOrPg().forEach(visitor::visit);
				if(utt.getT() != null)
					visitor.visit(utt.getT());
				utt.getPostcode().forEach(visitor::visit);
				retVal.addGroup(visitor.getOrthography());
			} else if(uttGrp instanceof String) {
				final Orthography ortho = new Orthography();
				ortho.putExtension(UnvalidatedValue.class, new UnvalidatedValue(uttGrp.toString()));
				retVal.addGroup(ortho);
			}
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
	 * @param itt
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
		final Tier<IPATranscript> ipaT = record.getIPATargetTier();
		final Tier<IPATranscript> ipaA = record.getIPAActualTier();

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
	 * @param stream
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
					&& evt.asStartElement().getAttributeByName(new QName("version")).getValue().equals("1.3");
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
		return (args) -> { return new XmlSessionReaderV1_3(); };
	}

}

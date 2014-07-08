package ca.phon.session.io.xml.v12;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.Comment;
import ca.phon.session.CommentEnum;
import ca.phon.session.MediaSegment;
import ca.phon.session.MediaUnit;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Sex;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionReader;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import ca.phon.xml.XMLObjectReader;
import ca.phon.xml.annotation.XMLSerial;

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
		name="Phon 1.4-1.6"
)
public class XMLSessionReader_v12 implements SessionReader, XMLObjectReader<Session> {
	
	private static final Logger LOGGER = Logger
			.getLogger(XMLSessionReader_v12.class.getName());

	@Override
	public Session read(Document doc, Element ele)
			throws IOException {
		Session retVal = null;
		
		final ObjectFactory xmlFactory = new ObjectFactory();
		
		try {
			// TODO JAXB is good enough for reading in this type of session
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
				final DateTime dateTime = new DateTime(
						xmlDate.getYear(),
						xmlDate.getMonth(),
						xmlDate.getDay(), 12, 0);
				retVal.setDate(dateTime);
			}
			if(headerData.getLanguage().size() > 0) {
				String langs = "";
				for(String lang:headerData.getLanguage()) {
					langs += (langs.length() > 0 ? ", " : "") + lang;
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
						record = copyRecord(factory, rt);
					} catch (Exception e) {
						LOGGER.info(rt.getId());
						LOGGER.log(Level.SEVERE,
								e.getLocalizedMessage(), e);
						
					}
					
					try {
						if(rt.getId() != null) {
							UUID uuid = UUID.fromString(rt.getId());
							record.setUuid(uuid);
						}
					} catch (IllegalArgumentException e) {
					}
					
					record.setExcludeFromSearches(rt.isExcludeFromSearches());
					
					if(rt.getSpeaker() != null) {
						final ParticipantType pt = (ParticipantType)rt.getSpeaker();
						for(int pIdx = 0; pIdx < retVal.getParticipantCount(); pIdx++) {
							final Participant participant = retVal.getParticipant(pIdx);
							if(participant.getName() != null  && participant.getName().equals(pt.getName())) {
								record.setSpeaker(participant);
								break;
							} else if(participant.getId() != null && participant.getId().equals(pt.getId())) {
								record.setSpeaker(participant);
								break;
							}
						}
					}
					
					retVal.addRecord(record);
					
					for(Comment comment:recordComments) {
						record.addComment(comment);
					}
					recordComments.clear();
				}
			}
		}
		
		return retVal;
	}
	
	// participants
	private Participant copyParticipant(SessionFactory factory, ParticipantType pt, DateTime sessionDate) {
		final Participant retVal = factory.createParticipant();
		
		retVal.setId(pt.getId());
		retVal.setName(pt.getName());
		
		final XMLGregorianCalendar bday = pt.getBirthday();
		if(bday != null) {
			final DateTime bdt = new DateTime(bday.getYear(), bday.getMonth(), bday.getDay(), 12, 0);
			retVal.setBirthDate(bdt);
			
			// calculate age up to the session date
			final Period period = new Period(bdt, sessionDate);
			retVal.setAgeTo(period);
		}
		
		retVal.setEducation(pt.getEducation());
		retVal.setGroup(pt.getGroup());
		
		String langs = "";
		for(String lang:pt.getLanguage())
			langs += (langs.length() > 0 ? ", " : "") + lang;
		retVal.setLanguage(langs);

		retVal.setSex(pt.getSex() == SexType.MALE ? Sex.MALE : Sex.FEMALE);
		
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
		return factory.createTierDescription(name, grouped);
	}
	
	private TierViewItem copyTierViewItem(SessionFactory factory, TvType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();
		
		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	// copy comment data
	private Comment copyComment(SessionFactory factory, CommentType ct) {
		final CommentEnum type = CommentEnum.fromString(ct.getType());
		final String value = ct.getContent();
		return factory.createComment(type, value);
	}
	
	private Record copyRecord(SessionFactory factory, RecordType rt) {
		final Record retVal = factory.createRecord();
		
		retVal.setExcludeFromSearches(rt.isExcludeFromSearches());
		
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
						e.printStackTrace();
					}
				}
				gidx++;
			}
		}
		
		// notes
		if(rt.getNotes() != null)
			retVal.getNotes().setGroup(0, rt.getNotes().getContent());
		
		// segment
		if(rt.getSegment() != null) {
			final MediaSegment segment = factory.createMediaSegment();
			segment.setStartValue(rt.getSegment().getStartTime());
			segment.setEndValue(rt.getSegment().getStartTime() + rt.getSegment().getDuration());
			segment.setUnitType(MediaUnit.Millisecond);
			
			retVal.getSegment().setGroup(0, segment);
		}
		
		// alignment
		for(AlignmentTierType att:rt.getAlignment()) {
			final Tier<PhoneMap> alignment = copyAlignment(factory, retVal, att);
			retVal.setPhoneAlignment(alignment);
			break; // only processing the first alignment element (which should be the only one)
		}
		
		// user tiers
		for(FlatTierType ftt:rt.getFlatTier()) {
			final Tier<String> flatTier = factory.createTier(ftt.getTierName(), String.class, false);
			flatTier.setGroup(0, ftt.getContent());
			retVal.putTier(flatTier);
		}
		
		for(GroupTierType gtt:rt.getGroupTier()) {
			final Tier<String> groupTier = factory.createTier(gtt.getTierName(), String.class, true);
			int gidx = 0;
			for(TgType tgt:gtt.getTg()) {
				final StringBuffer buffer = new StringBuffer();
				for(WordType wt:tgt.getW()) {
					if(buffer.length() > 0)
						buffer.append(" ");
					buffer.append(wt.getContent());
				}
				groupTier.setGroup(gidx++, buffer.toString());
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
					eleList.add("(" + oct.getContent() + ")");
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
			
			final Orthography ortho = new Orthography(buffer.toString());
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
				
				try {
					final IPATranscript transcript = IPATranscript.parseIPATranscript(groupBuffer.toString());
					
					// copy syllabification if transcript is the same size as our provided syllabification
					if(pt.getSb() != null && transcript.length() == pt.getSb().getPh().size()) {
						final CopyTranscriptVisitor visitor = new CopyTranscriptVisitor(pt.getSb().getPh());
						transcript.accept(visitor);
					}
					
					retVal.addGroup(transcript);
				} catch (ParseException pe) {
					LOGGER.log(Level.SEVERE, pe.getLocalizedMessage(), pe);
					retVal.addGroup(new IPATranscript());
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
				final SyllableConstituentType scType = SyllableConstituentType.fromString(ctt.toString());
				if(scType != null) {
					cp.setScType(scType);
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
		// indicies
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
				alignmentData[0][i] = mt.getValue().get(0);
				alignmentData[1][i] = mt.getValue().get(1);
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
}

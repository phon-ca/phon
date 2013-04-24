package ca.phon.session.io.xml.v12;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.phon.ipa.CompoundPhone;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.Phone;
import ca.phon.orthography.Orthography;
import ca.phon.session.Comment;
import ca.phon.session.CommentEnum;
import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Sex;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;
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
public class XMLSessionReader_v12 implements XMLObjectReader<Session> {

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
					unmarshaller.unmarshal(doc, SessionType.class);
			
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
		if(headerData.getMedia() != null && headerData.getMedia().length() > 0) {
			retVal.setMediaLocation(headerData.getMedia());
		}
		if(headerData.getDate() != null) {
			final XMLGregorianCalendar xmlDate = headerData.getDate();
			final DateTime dateTime = new DateTime(xmlDate);
			retVal.setDate(dateTime);
		}
		if(headerData.getLanguage().size() > 0) {
			String langs = "";
			for(String lang:headerData.getLanguage()) {
				langs += (langs.length() > 0 ? ", " : "") + lang;
			}
			retVal.setLanguage(langs);
		}
		
		// copy participant information
		final ParticipantsType participants = sessionType.getParticipants();
		for(ParticipantType pt:participants.getParticipant()) {
			final Participant p = copyParticipant(factory, pt);
			retVal.addParticipant(p);
		}
		
		// copy transcriber information
		final TranscribersType transcribers = sessionType.getTranscribers();
		for(TranscriberType tt:transcribers.getTranscriber()) {
			final Transcriber t = copyTranscriber(factory, tt);
			retVal.addTranscriber(t);
		}
		
		// copy tier information
		final UserTiersType userTiers = sessionType.getUserTiers();
		for(UserTierType utt:userTiers.getUserTier()) {
			final TierDescription td = copyTierDescription(factory, utt);
			retVal.addUserTier(td);
		}
		
		final List<TierViewItem> tierOrder = new ArrayList<TierViewItem>();
		for(TvType tot:sessionType.getTierOrder().getTier()) {
			final TierViewItem toi = copyTierViewItem(factory, tot);
			tierOrder.add(toi);
		}
		retVal.setTierView(tierOrder);
		
		// copy transcript data
		final List<Comment> recordComments = new ArrayList<Comment>();
		for(Object uOrComment:sessionType.getTranscript().getUOrComment()) {
			if(uOrComment instanceof CommentType) {
				final CommentType ct = (CommentType)uOrComment;
				final Comment comment = copyComment(factory, ct);
				recordComments.add(comment);
			} else {
				final RecordType rt = (RecordType)uOrComment;
				final Record record = copyRecord(factory, rt);
				retVal.addRecord(record);
				
				for(Comment comment:recordComments) {
					record.addComment(comment);
				}
				recordComments.clear();
			}
		}
		
		return retVal;
	}
	
	// participants
	private Participant copyParticipant(SessionFactory factory, ParticipantType pt) {
		final Participant retVal = factory.createParticipant();
		
		retVal.setId(pt.getId());
		retVal.setName(pt.getName());
		
		final XMLGregorianCalendar bday = pt.getBirthday();
		if(bday != null) {
			final DateTime bdt = new DateTime(bday);
			retVal.setBirthDate(bdt);
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
		retVal.setPassword(tt.getPassword().getContent());
		retVal.setUsePassword(tt.getPassword().isUse());
		
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
		
		// notes
		if(rt.getNotes() != null)
			retVal.getNotes().setGroup(0, rt.getNotes().getContent());
		
		// segment
		
		
		// alignment
		
		// user tiers
		
		
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
		
		final Tier<Orthography> retVal = factory.createTier(Orthography.class);
		
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
				} else if(ele instanceof ChatCodeType) {
					ChatCodeType cc = (ChatCodeType)ele;
					eleList.add("<" + cc.getContent() + ">");
				} else if(ele instanceof InnerGroupMarker) {
					InnerGroupMarker ig = (InnerGroupMarker)ele;
					if(ig.getType() == InnerGroupMarkerType.S)
						eleList.add("{");
					else
						eleList.add("}");
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
		final Tier<IPATranscript> retVal = factory.createTier(IPATranscript.class);
		
		// attempt an exact copy first
		for(PhoType pt:itt.getPg()) {
			final StringBuffer groupBuffer = new StringBuffer();
			for(WordType wt:pt.getW()) {
				if(groupBuffer.length() > 0)
					groupBuffer.append(" ");
				groupBuffer.append(wt.getContent());
			}
			
			try {
				final IPATranscript transcript = IPATranscript.parseTranscript(groupBuffer.toString());
				
				// copy syllabification if transcript is the same size as our provided syllabification
				if(transcript.size() == pt.getSb().getPh().size()) {
					final CopyTranscriptVisitor visitor = new CopyTranscriptVisitor(pt.getSb().getPh());
					transcript.accept(visitor);
				}
				
				retVal.addGroup(transcript);
			} catch (ParseException pe) {
				pe.printStackTrace();
				
				retVal.addGroup(new IPATranscript());
			}
		}
		
		return retVal;
	}

	private class CopyTranscriptVisitor extends VisitorAdapter<IPAElement> {
		
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
				if(scType != null) {
					phone.setScType(scType);
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
	
}

package ca.phon.session.io.xml.v12;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;

import ca.phon.alignment.PhoneMap;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.xml.IpaType;
import ca.phon.orthography.OrthoElement;
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
import ca.phon.session.io.SessionWriter;
import ca.phon.xml.annotation.XMLSerial;

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
public class XMLSessionWriter_v12 implements SessionWriter {
	
	private final static Logger LOGGER = Logger.getLogger(XMLSessionWriter_v12.class.getName());

	/**
	 * Create a new jaxb version of the session
	 * 
	 * @param session
	 * @return an version of the session use-able by jaxb 
	 */
	private JAXBElement<SessionType> toSessionType(Session session) throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		final SessionType retVal = factory.createSessionType();
		
		// header data
		retVal.setId(session.getName());
		retVal.setCorpus(session.getCorpus());
		
		final HeaderType headerData = factory.createHeaderType();
		if(session.getMediaLocation().length() > 0) {
			headerData.setMedia(session.getMediaLocation());
		}
		final DateTime date = session.getDate();
		try {
			final DatatypeFactory df = DatatypeFactory.newInstance();
			final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(date.toGregorianCalendar());
			cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			headerData.setDate(cal);
		} catch (DatatypeConfigurationException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		final String lang = session.getLanguage();
		final String langs[] = lang.split(",");
		for(String l:langs) {
			headerData.getLanguage().add(l);
		}
		retVal.setHeader(headerData);
		
		// participants
		final ParticipantsType parts = factory.createParticipantsType();
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant part = session.getParticipant(i);
			final ParticipantType pt = copyParticipant(factory, part);
			parts.getParticipant().add(pt);
		}
		retVal.setParticipants(parts);
		
		// transcribers
		final TranscribersType tt = factory.createTranscribersType();
		for(int i = 0; i < session.getTranscriberCount(); i++) {
			final Transcriber tr = session.getTranscriber(i);
			final TranscriberType trt = copyTranscriber(factory, tr);
			tt.getTranscriber().add(trt);
		}
		retVal.setTranscribers(tt);
		
		// tier info/ordering
		final UserTiersType utt = factory.createUserTiersType();
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			final UserTierType tierType = copyTierDescription(factory, td);
			utt.getUserTier().add(tierType);
		}
		retVal.setUserTiers(utt);
		
		final TierOrderType tot = factory.createTierOrderType();
		for(TierViewItem tvi:session.getTierView()) {
			final TvType tvt = copyTierViewItem(factory, tvi);
			tot.getTier().add(tvt);
		}
		retVal.setTierOrder(tot);
		
		final TranscriptType transcript = factory.createTranscriptType();
		// session data
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record record = session.getRecord(i);
			
			// insert comments first
			for(int j = 0; j < record.getNumberOfComments(); j++) {
				final Comment com = record.getComment(j);
				final CommentType ct = copyComment(factory, com);
				transcript.getUOrComment().add(ct);
			}
			
			// copy record data
			final RecordType rt = copyRecord(factory, record);
			transcript.getUOrComment().add(rt);
		}
		retVal.setTranscript(transcript);
		
		return factory.createSession(retVal);
	}
	
	/**
	 * copy participant info
	 */
	private ParticipantType copyParticipant(ObjectFactory factory, Participant part) {
		final ParticipantType retVal = factory.createParticipantType();
		
		retVal.setId(part.getId());
		retVal.setName(part.getName());
		
		final DateTime bday = part.getBirthDate();
		if(bday != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(bday.toGregorianCalendar());
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				retVal.setBirthday(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.log(Level.WARNING, e.toString(), e);
			}
		}
		
		retVal.setEducation(part.getEducation());
		retVal.setGroup(part.getGroup());
		
		final String lang = part.getLanguage();
		final String langs[] = lang.split(",");
		for(String l:langs) {
			retVal.getLanguage().add(l);
		}

		retVal.setSex(part.getSex() == Sex.MALE ? SexType.MALE : SexType.FEMALE);
		
		ParticipantRole prole = part.getRole();
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole.toString());
		
		retVal.setSES(part.getSES());
			
		return retVal;
	}
	
	/**
	 * copy transcriber
	 */
	private TranscriberType copyTranscriber(ObjectFactory factory, Transcriber tr) {
		final TranscriberType retVal = factory.createTranscriberType();
		
		retVal.setId(tr.getUsername());
		retVal.setName(tr.getRealName());
		final PasswordType pst = factory.createPasswordType();
		pst.setContent(tr.getPassword());
		pst.setUse(tr.usePassword());
		retVal.setPassword(pst);
		
		return retVal;
	}
	
	// tier descriptions
	private UserTierType copyTierDescription(ObjectFactory factory, TierDescription td) {
		final boolean grouped = td.isGrouped();
		final String name = td.getName();
		
		final UserTierType retVal = factory.createUserTierType();
		retVal.setGrouped(grouped);
		retVal.setTierName(name);
		return retVal;
	}
	
	private TvType copyTierViewItem(ObjectFactory factory, TierViewItem tvi) {
		final boolean locked = tvi.isTierLocked();
		final boolean visible = tvi.isVisible();
		final String name = tvi.getTierName();
		final String font = tvi.getTierFont();
		
		final TvType retVal = factory.createTvType();
		retVal.setLocked(locked);
		retVal.setFont(font);
		retVal.setTierName(name);
		retVal.setVisible(visible);
		return retVal;
	}
	
	// copy comment data
	private CommentType copyComment(ObjectFactory factory, Comment com) {
		final CommentEnum type = com.getType();
		final String value = com.getValue();
		
		final CommentType retVal = factory.createCommentType();
		retVal.setContent(value);
		retVal.setType(type.toString());
		return retVal;
	}
	
	// record
	private RecordType copyRecord(ObjectFactory factory, Record record) {
		final RecordType retVal = factory.createRecordType();
		
		retVal.setExcludeFromSearches(record.isExcludeFromSearches());
		
		// orthography
		final Tier<Orthography> orthoTier = record.getOrthography();
		final OrthographyType orthoType = copyOrthography(factory, orthoTier);
		retVal.setOrthography(orthoType);
		
		// ipa
		final Tier<IPATranscript> ipaTarget = record.getIPATarget();
		final IpaTierType targetType = copyIPA(factory, ipaTarget);
		targetType.setForm(PhoTypeType.MODEL);
		retVal.getIpaTier().add(targetType);
		
		final Tier<IPATranscript> ipaActual = record.getIPAActual();
		final IpaTierType actualType = copyIPA(factory, ipaActual);
		actualType.setForm(PhoTypeType.ACTUAL);
		retVal.getIpaTier().add(actualType);
		
		// TODO blind transcriptions
//		for(BlindTierType btt:rt.getBlindTranscription()) {
//			// get the correct ipa object from our new record
//			final Tier<IPATranscript> ipaTier = 
//					(btt.getForm() == PhoTypeType.MODEL ? retVal.getIPATarget() : retVal.getIPAActual());
//			int gidx = 0;
//			for(BgType bgt:btt.getBg()) {
//				final StringBuffer buffer = new StringBuffer();
//				for(WordType wt:bgt.getW()) {
//					if(buffer.length() > 0) 
//						buffer.append(" ");
//					buffer.append(wt.getContent());
//				}
//				
//				final IPATranscript ipa = (gidx < ipaTier.numberOfGroups() ? ipaTier.getGroup(gidx) : null);
//				
//				if(ipa != null) {
//					try {
//						final IPATranscript blindTranscript = 
//								IPATranscript.parseTranscript(buffer.toString());
//						final String name = btt.getUser().toString();
//						
//						AlternativeTranscript at = ipa.getExtension(AlternativeTranscript.class);
//						if(at == null) {
//							at = new AlternativeTranscript();
//							ipa.putExtension(AlternativeTranscript.class, at);
//						}
//						at.put(name, blindTranscript);
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//				}
//				gidx++;
//			}
//		}
		
		// notes
		final Tier<String> notesTier = record.getNotes();
		if(notesTier.getGroup(0).length() > 0) {
			final FlatTierType notesType = factory.createFlatTierType();
			notesType.setContent(notesTier.getGroup(0));
			notesType.setTierName(notesTier.getName());
			retVal.setNotes(notesType);
		}
		
		// segment
		if(record.getSegment() != null) {
			final SegmentType segType = factory.createSegmentType();
			final MediaSegment segment = record.getSegment();
			
			segType.setDuration(segment.getEndValue() - segment.getStartValue());
			segType.setStartTime(segment.getStartValue());
			segType.setUnitType(SegmentUnitType.MS);
			
			retVal.setSegment(segType);
		}
		
		// alignment
		final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
		if(alignmentTier != null) {
			final AlignmentTierType att = copyAlignment(factory, record, alignmentTier);
			retVal.getAlignment().add(att);
		}
		
		for(String tierName:record.getTierNames()) {
			final Tier<String> userTier = record.getTier(tierName, String.class);
			if(userTier.isGrouped()) {
				// grouped tiers
				final GroupTierType gtt = factory.createGroupTierType();
				gtt.setTierName(tierName);
				for(String groupVal:userTier) {
					final TgType tg = factory.createTgType();
					final String[] words = groupVal.split("\\p{Space}");
					for(String word:words) {
						final WordType wt = factory.createWordType();
						wt.setContent(word);
						tg.getW().add(wt);
					}
					gtt.getTg().add(tg);
				}
				retVal.getGroupTier().add(gtt);
			} else {
				// flat tiers
				final FlatTierType ftt = factory.createFlatTierType();
				ftt.setTierName(tierName);
				ftt.setContent(userTier.getGroup(0));
			}
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
	private OrthographyType copyOrthography(ObjectFactory factory, Tier<Orthography> orthoTier) {
		final OrthographyType retVal = factory.createOrthographyType();

		for(Orthography ortho:orthoTier) {
			final OrthoToXmlVisitor visitor = new OrthoToXmlVisitor();
			ortho.accept(visitor);
			final GroupType gt = visitor.getGroup();
			retVal.getWOrGOrP().add(gt);
		}
		
		return retVal;
	}
	
	/**
	 * Copy ipa
	 * 
	 * @param ipaTier
	 * @return {@link IpaTierType}
	 */
	public IpaTierType copyIPA(ObjectFactory factory, Tier<IPATranscript> ipaTier) {
		final IpaTierType retVal = factory.createIpaTierType();
		
		for(IPATranscript ipa:ipaTier) {
			final IpaToXmlVisitor visitor = new IpaToXmlVisitor();
			ipa.accept(visitor);
			final PhoType pt = visitor.getPho();
			retVal.getPg().add(pt);
		}
		
		return retVal;
	}
	
	/**
	 * Copy alignment data
	 */
	private AlignmentTierType copyAlignment(ObjectFactory factory, Record record, Tier<PhoneMap> alignmentTier) {
		final AlignmentTierType retVal = factory.createAlignmentTierType();
		
		retVal.setType(AlignmentTypeType.SEGMENTAL);
		for(PhoneMap pm:alignmentTier) {
			final AlignmentType at = factory.createAlignmentType();
			at.setLength(pm.getAlignmentLength());
			
			final Integer[] top = pm.getTopAlignment();
			final Integer[] btm = pm.getBottomAlignment();
			for(int i = 0; i < pm.getAlignmentLength(); i++) {
				final MappingType mapping = factory.createMappingType();
				mapping.getValue().add(top[i]);
				mapping.getValue().add(btm[i]);
				at.getPhomap().add(mapping);
			}
			retVal.getAg().add(at);
		}
		
		return retVal;
	}
	
	@Override
	public void writeSession(Session session, OutputStream out)
			throws IOException {
		final JAXBElement<SessionType> ele = toSessionType(session);
		
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
			marshaller.marshal(ele, out);
		} catch(JAXBException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}

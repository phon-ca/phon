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
package ca.phon.session.io.xml.v13;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.SerializationWarning;
import ca.phon.session.io.SerializationWarnings;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionWriter;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

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
		name="Phon 3.6+ (.xml)"
)
@Rank(0)
public class XMLSessionWriter_v13 implements SessionWriter, IPluginExtensionPoint<SessionWriter> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XMLSessionWriter_v13.class.getName());

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
		retVal.setVersion("1.3");
		retVal.setId(session.getName());
		retVal.setCorpus(session.getCorpus());

		final HeaderType headerData = factory.createHeaderType();
		if(session.getMediaLocation() != null && session.getMediaLocation().length() > 0) {
			headerData.setMedia(session.getMediaLocation());
		}
		final LocalDate date = session.getDate();
		if(date != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(
						GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault())));
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				headerData.setDate(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}
		final String lang = session.getLanguage();
		if(lang != null && lang.length() > 0) {
			final String langs[] = lang.split("\\p{Space}");
			for(String l:langs) {
				headerData.getLanguage().add(l);
			}
		}
		retVal.setHeader(headerData);

		final TranscriptType transcript = factory.createTranscriptType();
		// commets
		for(int i = 0; i < session.getMetadata().getNumberOfComments(); i++) {
			final Comment c = session.getMetadata().getComment(i);
			final CommentType ct = copyComment(factory, c);
			transcript.getUOrComment().add(ct);
		}

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
			try {
				final RecordType rt = copyRecord(factory, retVal, record);
	
				rt.setId(record.getUuid().toString());
	
				if(record.isExcludeFromSearches())
					rt.setExcludeFromSearches(record.isExcludeFromSearches());
	
				// setup participant
				if(record.getSpeaker() != null) {
					for(ParticipantType pt:parts.getParticipant()) {
						if(pt.getId().equals(record.getSpeaker().getId())) {
							rt.setSpeaker(pt);
							break;
						}
					}
				}
	
				transcript.getUOrComment().add(rt);
			} catch (Exception e) {
				// catch all record-specific errors and recover
				LOGGER.error( "Record #" + (i+1) + " " + e.getLocalizedMessage(), e);
				SerializationWarnings warnings = session.getExtension(SerializationWarnings.class);
				if(warnings == null) {
					warnings = new SerializationWarnings();
					session.putExtension(SerializationWarnings.class, warnings);
				}
				warnings.add(new SerializationWarning(i, e));
			}
		}
		retVal.setTranscript(transcript);

		return factory.createSession(retVal);
	}

	private int pIdx = 0;
	/**
	 * copy participant info
	 */
	private ParticipantType copyParticipant(ObjectFactory factory, Participant part) {
		final ParticipantType retVal = factory.createParticipantType();

		if(part.getId() != null)
			retVal.setId(part.getId());

		retVal.setName(part.getName());

		final LocalDate bday = part.getBirthDate();
		if(bday != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(
						GregorianCalendar.from(bday.atStartOfDay(ZoneId.systemDefault())));
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				retVal.setBirthday(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.warn( e.toString(), e);
			}
		}

		if(part.getBirthplace() != null) {
			retVal.setBirthplace(part.getBirthplace());
		}

		final Period age = part.getAge(null);
		if(age != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final Duration ageDuration = df.newDuration(true, age.getYears(), age.getMonths(), age.getDays(), 0, 0, 0);
				retVal.setAge(ageDuration);
			} catch (DatatypeConfigurationException e) {
				LOGGER.warn( e.toString(), e);
			}
		}

		retVal.setEducation(part.getEducation());
		retVal.setGroup(part.getGroup());

		final String lang = part.getLanguage();
		final String langs[] = (lang != null ? lang.split(",") : new String[0]);
		for(String l:langs) {
			retVal.getLanguage().add(StringUtils.strip(l));
		}

		if(part.getFirstLanguage() != null)
			retVal.setFirstLanguage(part.getFirstLanguage());

		if(part.getSex() == Sex.MALE)
			retVal.setSex(SexType.MALE);
		else if(part.getSex() == Sex.FEMALE)
			retVal.setSex(SexType.FEMALE);

		ParticipantRole prole = part.getRole();
		if(prole == null)
			prole = ParticipantRole.TARGET_CHILD;
		retVal.setRole(prole.toString());

		// create ID based on role if possible
		if(retVal.getId() == null && prole != null) {
			if(prole == ParticipantRole.TARGET_CHILD) {
				retVal.setId("CHI");
			} else if(prole == ParticipantRole.MOTHER) {
				retVal.setId("MOT");
			} else if(prole == ParticipantRole.FATHER) {
				retVal.setId("FAT");
			} else if(prole == ParticipantRole.INTERVIEWER) {
				retVal.setId("INT");
			} else {
				retVal.setId("p" + (++pIdx));
			}
		}

		retVal.setSES(part.getSES());

		if(part.getOther() != null)
			retVal.setOther(part.getOther());

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
		if(td.getDeclaredType() != TierString.class)
			retVal.setType(td.getDeclaredType().getName());
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
	
	private SegmentType copySegment(ObjectFactory factory, MediaSegment segment) {
		final SegmentType segType = factory.createSegmentType();
		segType.setDuration(segment.getEndValue() - segment.getStartValue());
		segType.setStartTime(segment.getStartValue());
		segType.setUnitType(SegmentUnitType.MS);
		return segType;
	}

	private GroupSegment copyGroupSegment(ObjectFactory factory, ca.phon.session.GroupSegment groupSegment) {
		final GroupSegment retVal = factory.createGroupSegment();
		retVal.setStart(groupSegment.getStart());
		retVal.setEnd(groupSegment.getEnd());
		return retVal;
	}

	// copy comment data
	private CommentType copyComment(ObjectFactory factory, Comment com) {
		final String tag = com.getTag();
		final String value = com.getValue();

		final CommentType retVal = factory.createCommentType();
		retVal.getContent().add(value);
		if(com.getExtension(MediaSegment.class) != null) {
			retVal.getContent().add(
					factory.createSegment(copySegment(factory, (MediaSegment)com.getExtension(MediaSegment.class)))
			);
		}
		retVal.setType(tag);
		return retVal;
	}

	// record
	private RecordType copyRecord(ObjectFactory factory, SessionType session, Record record) {
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

		// blind transcriptions
		final Map<String, BlindTierType> actualBlindTiers = new HashMap<String, BlindTierType>();
		final Map<String, BlindTierType> targetBlindTiers = new HashMap<String, BlindTierType>();
		final Map<String, Boolean> hasActualData = new HashMap<String, Boolean>();
		final Map<String, Boolean> hasTargetData = new HashMap<String, Boolean>();

		for(TranscriberType tr:session.getTranscribers().getTranscriber()) {
			final BlindTierType targetBt = factory.createBlindTierType();
			targetBt.setUser(tr);
			targetBt.setForm(PhoTypeType.MODEL);
			targetBlindTiers.put(tr.getId(), targetBt);
			hasTargetData.put(tr.getId(), false);

			final BlindTierType actualBt = factory.createBlindTierType();
			actualBt.setUser(tr);
			actualBt.setForm(PhoTypeType.ACTUAL);
			actualBlindTiers.put(tr.getId(), actualBt);
			hasActualData.put(tr.getId(), false);
		}

		for(IPATranscript ipa:ipaTarget) {
			final AlternativeTranscript blindTranscripts = ipa.getExtension(AlternativeTranscript.class);

			for(TranscriberType tr:session.getTranscribers().getTranscriber()) {
				final BlindTierType btt = targetBlindTiers.get(tr.getId());
				final BgType bg = factory.createBgType();

				if(blindTranscripts != null && blindTranscripts.containsKey(tr.getId())) {
					final IPATranscript blindTranscript = blindTranscripts.get(tr.getId());
					final String[] words = blindTranscript.toString().split("\\p{Space}");
					for(String w:words) {
						final WordType wt = factory.createWordType();
						if(w == null) w = new String();
						wt.setContent(w);
						bg.getW().add(wt);
					}
					hasTargetData.put(tr.getId(), true);
				}
				btt.getBg().add(bg);
			}
		}

		for(IPATranscript ipa:ipaActual) {
			final AlternativeTranscript blindTranscripts = ipa.getExtension(AlternativeTranscript.class);

			for(TranscriberType tr:session.getTranscribers().getTranscriber()) {
				final BlindTierType btt = actualBlindTiers.get(tr.getId());
				final BgType bg = factory.createBgType();

				if(blindTranscripts != null && blindTranscripts.containsKey(tr.getId())) {
					final IPATranscript blindTranscript = blindTranscripts.get(tr.getId());
					final String[] words = blindTranscript.toString().split("\\p{Space}");
					for(String w:words) {
						final WordType wt = factory.createWordType();
						wt.setContent(w);
						bg.getW().add(wt);
					}
					hasActualData.put(tr.getId(), true);
				}
				btt.getBg().add(bg);
			}
		}

		for(String trId:hasActualData.keySet()) {
			if(hasActualData.get(trId)) {
				final BlindTierType btt = actualBlindTiers.get(trId);
				retVal.getBlindTranscription().add(btt);
			}
		}

		for(String trId:hasTargetData.keySet()) {
			if(hasTargetData.get(trId)) {
				final BlindTierType btt = targetBlindTiers.get(trId);
				retVal.getBlindTranscription().add(btt);
			}
		}

		// notes
		final Tier<TierString> notesTier = record.getNotes();
		if(notesTier.numberOfGroups() > 0 && notesTier.getGroup(0) != null && notesTier.getGroup(0).length() > 0) {
			final FlatTierType notesType = factory.createFlatTierType();
			notesType.setContent(notesTier.getGroup(0).toString());
			notesType.setTierName(notesTier.getName());
			retVal.setNotes(notesType);
		}

		// segment
		final SegmentType segType = copySegment(factory, record.getMediaSegment());
		retVal.setSegment(segType);
		if(record.getGroupSegment().numberOfGroups() > 0) {
			for(ca.phon.session.GroupSegment groupSegment:record.getGroupSegment()) {
				segType.getGseg().add(copyGroupSegment(factory, groupSegment));
			}
		}

		// alignment
		final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
		if(alignmentTier != null) {
			final AlignmentTierType att = copyAlignment(factory, record, alignmentTier);
			retVal.getAlignment().add(att);
		}

		for(String tierName:record.getExtraTierNames()) {
			Tier<TierString> userTier = record.getTier(tierName, TierString.class);
			if(userTier == null)
				userTier = SessionFactory.newFactory().createTier(tierName, TierString.class, true);

			if(userTier.isGrouped()) {
				// grouped tiers
				final GroupTierType gtt = factory.createGroupTierType();
				gtt.setTierName(tierName);
				for(TierString groupVal:userTier) {
					final TgType tg = factory.createTgType();
					for(TierString word:groupVal.getWords()) {
						final WordType wt = factory.createWordType();
						wt.setContent(word.toString());
						tg.getW().add(wt);
					}
					gtt.getTg().add(tg);
				}
				retVal.getGroupTier().add(gtt);
			} else {
				// flat tiers
				final FlatTierType ftt = factory.createFlatTierType();
				ftt.setTierName(tierName);
				ftt.setContent(userTier.getGroup(0).toString());
				retVal.getFlatTier().add(ftt);
			}
		}

		return retVal;
	}

	/**
	 * Copy orthography
	 *
	 * @param factory
	 * @param orthoTier
	 * @return
	 */
	private OrthographyType copyOrthography(ObjectFactory factory, Tier<Orthography> orthoTier) {
		final OrthographyType retVal = factory.createOrthographyType();

		for(Orthography ortho:orthoTier) {
			final UnvalidatedValue uv = ortho.getExtension(UnvalidatedValue.class);
			if(ortho.length() == 0 && uv != null) {
				// stuff everything into a single word element
				// it will be marked invalid when read again, but we should not
				// delete user entered information
				final GroupType gt = factory.createGroupType();
				final WordType wt = factory.createWordType();
				wt.setContent(uv.getValue());
				gt.getWOrComOrE().add(wt);
				retVal.getWOrGOrP().add(gt);
			} else {
				final OrthoToXmlVisitor visitor = new OrthoToXmlVisitor();
				ortho.accept(visitor);
				final GroupType gt = visitor.getGroup();
				retVal.getWOrGOrP().add(gt);
			}
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

			// XXX Check for unvalidated values first
			final UnvalidatedValue unvalidatedValue = ipa.getExtension(UnvalidatedValue.class);
			if(unvalidatedValue != null) {
				final PhoType pho = factory.createPhoType();
				final SyllabificationType sb = factory.createSyllabificationType();
				pho.setSb(sb);
				final WordType currentWord = factory.createWordType();
				currentWord.setContent(unvalidatedValue.getValue());
				pho.getW().add(currentWord);
				retVal.getPg().add(pho);
			} else {
				final IpaToXmlVisitor visitor = new IpaToXmlVisitor();
				ipa.accept(visitor);
				final PhoType pt = visitor.getPho();
				retVal.getPg().add(pt);
			}
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
			LOGGER.error( e.getMessage(), e);
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionWriter.class;
	}

	@Override
	public IPluginExtensionFactory<SessionWriter> getFactory() {
		return (args) -> { return new XMLSessionWriter_v13(); };
	}

}

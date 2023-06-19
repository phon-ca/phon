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
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.Terminator;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionWriter;
import ca.phon.util.Language;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

import javax.xml.datatype.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;

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
public class XmlSessionWriterV1_3 implements SessionWriter, IPluginExtensionPoint<SessionWriter> {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XmlSessionWriterV1_3.class.getName());

	/**
	 * Create a new jaxb version of the session
	 *
	 * @param session
	 * @return version of the session use-able by jaxb
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

		final List<Language> langs = session.getLanguages();
		for(Language lang:langs) {
			headerData.getLanguages().add(lang.toString());
		}
		retVal.setHeader(headerData);

		// participants
		final ParticipantsType parts = factory.createParticipantsType();
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant part = session.getParticipant(i);
			final ParticipantType pt = writeParticipant(factory, part);
			parts.getParticipant().add(pt);
		}
		retVal.setParticipants(parts);

		// transcribers
		final TranscribersType tt = factory.createTranscribersType();
		for(int i = 0; i < session.getTranscriberCount(); i++) {
			final Transcriber tr = session.getTranscriber(i);
			final TranscriberType trt = writeTranscriber(factory, tr);
			tt.getTranscriber().add(trt);
		}
		retVal.setTranscribers(tt);

		// tier info/ordering
		final UserTiersType utt = factory.createUserTiersType();
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			final TierDescriptionType tierType = writeTierDescription(factory, td);
			utt.getTd().add(tierType);
		}
		retVal.setUserTiers(utt);

		final TierOrderType tot = factory.createTierOrderType();
		for(TierViewItem tvi:session.getTierView()) {
			final TierViewType tvt = writeTierViewItem(factory, tvi);
			tot.getTv().add(tvt);
		}
		retVal.setTierOrder(tot);

		final TranscriptType transcript = factory.createTranscriptType();
		retVal.setTranscript(transcript);
		// session data
		for(int i = 0; i < session.getTranscript().getNumberOfElements(); i++) {
			final var ele = session.getTranscript().getElementAt(i);
			if(ele.isComment()) {
				final Comment comment = ele.asComment();
				final CommentType commentType = copyComment(factory, comment);
				transcript.getROrComment().add(commentType);
			} else if(ele.isRecord()) {
				final RecordType recordType = copyRecord(factory, retVal, ele.asRecord());
				transcript.getROrComment().add(recordType);
			} else {
				// should not happen
			}
		}
//		for(int i = 0; i < session.getRecordCount(); i++) {
//			final Record record = session.getRecord(i);
//
//			// insert comments first
//			for(int j = 0; j < record.getNumberOfComments(); j++) {
//				final Comment com = record.getComment(j);
//				final CommentType ct = copyComment(factory, com);
//				transcript.getROrComment().add(ct);
//			}
//
//			// copy record data
//			try {
//				final RecordType rt = copyRecord(factory, retVal, record);
//
//				rt.setId(record.getUuid().toString());
//
//				if(record.isExcludeFromSearches())
//					rt.setExcludeFromSearches(record.isExcludeFromSearches());
//
//				// setup participant
//				if(record.getSpeaker() != null) {
//					for(ParticipantType pt:parts.getParticipant()) {
//						if(pt.getId().equals(record.getSpeaker().getId())) {
//							rt.setSpeaker(pt);
//							break;
//						}
//					}
//				}
//
//				transcript.getROrComment().add(rt);
//			} catch (Exception e) {
//				// catch all record-specific errors and recover
//				LOGGER.error( "Record #" + (i+1) + " " + e.getLocalizedMessage(), e);
//				SerializationWarnings warnings = session.getExtension(SerializationWarnings.class);
//				if(warnings == null) {
//					warnings = new SerializationWarnings();
//					session.putExtension(SerializationWarnings.class, warnings);
//				}
//				warnings.add(new SerializationWarning(i, e));
//			}
//		}

		for(int tcIdx = 0; tcIdx < session.getMetadata().getNumberOfTrailingComments(); tcIdx++) {
			final Comment com = session.getMetadata().getTrailingComment(tcIdx);
			final CommentType ct = copyComment(factory, com);
			transcript.getROrComment().add(ct);
		}

		return factory.createSession(retVal);
	}

	private int pIdx = 0;
	/**
	 * copy participant info
	 */
	private ParticipantType writeParticipant(ObjectFactory factory, Participant part) {
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
			retVal.getLanguages().add(StringUtils.strip(l));
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
	private TranscriberType writeTranscriber(ObjectFactory factory, Transcriber tr) {
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
	private TierDescriptionType writeTierDescription(ObjectFactory factory, TierDescription td) {
		final TierDescriptionType retVal = factory.createTierDescriptionType();

		final String name = td.getName();
		retVal.setTierName(name);

		UserTierTypeType tierType = UserTierTypeType.SIMPLE;
		if(td.getDeclaredType() == Orthography.class) {
			tierType = UserTierTypeType.CHAT;
		} else if(td.getDeclaredType() == IPATranscript.class) {
			tierType = UserTierTypeType.IPA;
		}
		retVal.setType(tierType);

		for(String key:td.getTierParameters().keySet()) {
			final TierParameterType param = factory.createTierParameterType();
			param.setName(key);
			param.setContent(td.getTierParameters().get(key));
			retVal.getTierParameters().getParam().add(param);
		}

		if(td.getTierAlignmentRules().getType() != ca.phon.session.TierAlignmentRules.TierAlignmentType.None) {
			final TierAlignmentRules alignmentRules = factory.createTierAlignmentRules();
			for(var type:td.getTierAlignmentRules().getWordAlignmentRules().getAlignableTypes()) {
				final AlignableType at = switch(type) {
					case Freecode -> AlignableType.FREECODE;
					case Group -> AlignableType.GROUP;
					case InternalMedia -> AlignableType.INTERNAL_MEDIA;
					case Linker -> AlignableType.LINKER;
					case Pause -> AlignableType.PAUSE;
					case PhoneticGroup -> AlignableType.PHONETIC_GROUP;
					case Postcode -> AlignableType.POSTCODE;
					case TagMarker -> AlignableType.TAG_MARKER;
					case Terminator -> AlignableType.TERMINATOR;
					case Word -> AlignableType.WORD;
				};
				alignmentRules.getAlignWith().add(at);
			}
			alignmentRules.setIncludeExcluded(td.getTierAlignmentRules().getWordAlignmentRules().isIncludeExcluded());
			alignmentRules.setIncludeOmitted(td.getTierAlignmentRules().getWordAlignmentRules().isIncludeOmitted());
			alignmentRules.setIncludeXXX(td.getTierAlignmentRules().getWordAlignmentRules().isIncludeXXX());
			alignmentRules.setIncludeWWW(td.getTierAlignmentRules().getWordAlignmentRules().isIncludeWWW());
			alignmentRules.setIncludeYYY(td.getTierAlignmentRules().getWordAlignmentRules().isIncludeYYY());
			retVal.setTierAlignment(alignmentRules);
		}

		return retVal;
	}

	private TierViewType writeTierViewItem(ObjectFactory factory, TierViewItem tvi) {
		final boolean locked = tvi.isTierLocked();
		final boolean visible = tvi.isVisible();
		final String name = tvi.getTierName();
		final String font = tvi.getTierFont();

		final TierViewType retVal = factory.createTierViewType();
		retVal.setLocked(locked);
		retVal.setFont(font);
		retVal.setTierName(name);
		retVal.setVisible(visible);
		return retVal;
	}
	
	private MediaType writeSegment(ObjectFactory factory, MediaSegment segment) {
		final MediaType segType = factory.createMediaType();
		segType.setStart(BigDecimal.valueOf(segment.getStartValue()));
		segType.setEnd(BigDecimal.valueOf(segment.getEndValue()));
		final MediaUnitType unitType = switch (segment.getUnitType()) {
			case Second -> MediaUnitType.S;
			default -> MediaUnitType.MS;
		};
		segType.setUnit(unitType);
		return segType;
	}

	// copy comment data
	private CommentType copyComment(ObjectFactory factory, Comment com) {
		final CommentType retVal = factory.createCommentType();
		final CommentTypeType ct = switch (com.getType()) {
			case Activities -> CommentTypeType.ACTIVITIES;
			case Bck -> CommentTypeType.BCK;
			case Bg -> CommentTypeType.BEGIN_GEM;
			case Blank -> CommentTypeType.BLANK;
			case Date -> CommentTypeType.DATE;
			case Eg -> CommentTypeType.END_GEM;
			case G -> CommentTypeType.LAZY_GEM;
			case Generic -> CommentTypeType.GENERIC;
			case Location -> CommentTypeType.LOCATION;
			case NewEpisode -> CommentTypeType.NEW_EPISODE;
			case Number -> CommentTypeType.NUMBER;
			case Page -> CommentTypeType.PAGE;
			case RecordingQuality -> CommentTypeType.RECORDING_QUALITY;
			case RoomLayout -> CommentTypeType.ROOM_LAYOUT;
			case Situation -> CommentTypeType.SITUATION;
			case T -> CommentTypeType.T;
			case TapeLocation -> CommentTypeType.TAPE_LOCATION;
			case TimeDuration -> CommentTypeType.TIME_DURATION;
			case TimeStart -> CommentTypeType.TIME_START;
			case Transcriber -> CommentTypeType.TRANSCRIBER;
			case Transcription -> CommentTypeType.TRANSCRIPTION;
			case Types -> CommentTypeType.TYPES;
			case Warning -> CommentTypeType.WARNING;
		};
		retVal.setType(ct);

		if(com.getValue().getExtension(UnvalidatedValue.class) != null) {
			final UnvalidatedValue uv = com.getValue().getExtension(UnvalidatedValue.class);
			retVal.setUnparsed(writeUnparsed(factory, uv));
		} else {
			final UserTierData tierData = writeUserTierData(factory, com.getValue());
			retVal.setTierData(tierData);
		}

		return retVal;
	}

	private UnparsedData writeUnparsed(ObjectFactory factory, UnvalidatedValue unvalidatedValue) {
		final UnparsedData retVal = factory.createUnparsedData();
		retVal.setUv(unvalidatedValue.getValue());
		final ParseError pe = factory.createParseError();
		pe.setCharPositionInLine(unvalidatedValue.getParseError().getErrorOffset());
		pe.setContent(unvalidatedValue.getParseError().getMessage());
		retVal.setPe(pe);
		return retVal;
	}

	private UserTierData writeUserTierData(ObjectFactory factory, ca.phon.session.UserTierData tierData) {
		final UserTierData retVal = factory.createUserTierData();
		for(UserTierElement ele:tierData.getElements()) {
			if(ele instanceof TierString) {
				final TierWordType tw = factory.createTierWordType();
				tw.setContent(ele.toString());
				retVal.getTwOrTcOrInternalMedia().add(tw);
			} else if(ele instanceof UserTierComment) {
				final String commentText = ((UserTierComment)ele).text();
				final TierCommentType tc = factory.createTierCommentType();
				tc.setContent(commentText);
				retVal.getTwOrTcOrInternalMedia().add(tc);
			} else if(ele instanceof UserTierInternalMedia) {
				final UserTierInternalMedia im = (UserTierInternalMedia) ele;
				final MediaType mediaType = factory.createMediaType();
				mediaType.setStart(BigDecimal.valueOf(im.getInternalMedia().getStartTime()));
				mediaType.setEnd(BigDecimal.valueOf(im.getInternalMedia().getEndTime()));
				mediaType.setUnit(MediaUnitType.S);
				retVal.getTwOrTcOrInternalMedia().add(mediaType);
			}
		}
		return retVal;
	}

	// record
	private RecordType copyRecord(ObjectFactory factory, SessionType session, Record record) {
		final RecordType retVal = factory.createRecordType();

		retVal.setExcludeFromSearches(record.isExcludeFromSearches());

		// orthography
		final Tier<Orthography> orthoTier = record.getOrthographyTier();
		final OrthographyTierType orthoType = writeOrthographyTier(factory, orthoTier);
		retVal.setOrthography(orthoType);

		// ipa
		final Tier<IPATranscript> ipaTarget = record.getIPATargetTier();
		if(ipaTarget.hasValue()) {
			final IpaTierType targetType = writeIPATier(factory, ipaTarget);
			retVal.setIpaTarget(targetType);
		}

		final Tier<IPATranscript> ipaActual = record.getIPAActualTier();
		if(ipaActual.hasValue()) {
			final IpaTierType actualType = writeIPATier(factory, ipaActual);
			retVal.setIpaActual(actualType);
		}

		// notes
		final Tier<ca.phon.session.UserTierData> notesTier = record.getNotesTier();
		if(notesTier.hasValue()) {
			final NotesTierType notesTierType = writeNotesTier(factory, notesTier);
			retVal.setNotes(notesTierType);
		}

		// segment
		final MediaType segType = writeSegment(factory, record.getMediaSegment());
		retVal.setSegment(segType);

		// alignment
		final Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
		if(alignmentTier != null) {
			final AlignmentTierType att = writeAlignmentTier(factory, record, alignmentTier);
			retVal.setAlignment(att);
		}

		for(String tierName:record.getUserDefinedTierNames()) {
			Tier<ca.phon.session.UserTierData> userTier = record.getTier(tierName, ca.phon.session.UserTierData.class);
			if(userTier != null && userTier.hasValue() && userTier.getValue().getElements().size() > 0) {
				final UserTierType utt = writeUserTier(factory, userTier);

			}
		}

		return retVal;
	}

	private OrthographyTierType writeOrthographyTier(ObjectFactory factory, Tier<Orthography> orthoTier) {
		final OrthographyTierType retVal = factory.createOrthographyTierType();
		if(orthoTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, orthoTier.getUnvalidatedValue()));
		} else {
			// default utterance if action followed by terminator
			Orthography orthography = orthoTier.getValue().length() == 0
					? new Orthography(List.of(new ca.phon.orthography.Action(), new Terminator(ca.phon.orthography.TerminatorType.PERIOD)))
					: orthoTier.getValue();
			retVal.setU(writeOrthography(factory, orthography));
		}
		return retVal;
	}

	/**
	 *
	 * @param factory
	 * @param orthography
	 * @return
	 */
	private UtteranceType writeOrthography(ObjectFactory factory, Orthography orthography) {
		final OrthoToXmlVisitor visitor = new OrthoToXmlVisitor();
		orthography.accept(visitor);
		return visitor.getU();
	}

	/**
	 *
	 * @param ipaTier
	 * @return {@link IpaTierType}
	 */
	public IpaTierType writeIPATier(ObjectFactory factory, Tier<IPATranscript> ipaTier) {
		final IpaTierType retVal = factory.createIpaTierType();
		if(ipaTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, ipaTier.getUnvalidatedValue()));
		} else {
			retVal.setPho(writeIPA(factory, ipaTier.getValue()));
		}

		final AlternativeTranscript alternativeTranscripts = ipaTier.getExtension(AlternativeTranscript.class);
		if(alternativeTranscripts != null) {
			for(String transcriber:alternativeTranscripts.keySet()) {
				final BlindTranscriptionType btt = factory.createBlindTranscriptionType();
				btt.setTranscriber(transcriber);
				final IPATranscript ipa = alternativeTranscripts.get(transcriber);
				if(ipa.getExtension(UnvalidatedValue.class) != null) {
					btt.setUnparsed(writeUnparsed(factory, ipa.getExtension(UnvalidatedValue.class)));
				} else {
					btt.setPho(writeIPA(factory, ipa));
				}
				retVal.getBlindTranscription().add(btt);
			}
		}

		return retVal;
	}

	public PhoneticTranscriptionType writeIPA(ObjectFactory factory, IPATranscript ipa) {
		final IpaToXmlVisitor visitor = new IpaToXmlVisitor();
		ipa.accept(visitor);
		return visitor.getPho();
	}

	/**
	 * Copy alignment data
	 */
	private AlignmentTierType writeAlignmentTier(ObjectFactory factory, Record record, Tier<PhoneAlignment> alignmentTier) {
		final AlignmentTierType retVal = factory.createAlignmentTierType();
		int alignIdx = 0;
		for(PhoneMap pm:alignmentTier.getValue().getAlignments()) {
			int tidx = pm.getTopElements().length == 0 ? -1 : alignIdx;
			int aidx = pm.getBottomElements().length == 0 ? -1 : alignIdx;
			final PhoneMapType pmType = writePhoneMap(factory, pm);
			pmType.setTarget(BigInteger.valueOf(tidx));
			pmType.setActual(BigInteger.valueOf(aidx));
			retVal.getPm().add(pmType);
		}
		return retVal;
	}

	private PhoneMapType writePhoneMap(ObjectFactory factory, PhoneMap pm) {
		final PhoneMapType retVal = factory.createPhoneMapType();
		retVal.getTop().addAll(List.of(pm.getTopAlignment()));
		retVal.getBottom().addAll(List.of(pm.getBottomAlignment()));
		return retVal;
	}

	private NotesTierType writeNotesTier(ObjectFactory factory, Tier<ca.phon.session.UserTierData> notesTier) {
		final NotesTierType retVal = factory.createNotesTierType();
		if(notesTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, notesTier.getUnvalidatedValue()));
		} else {
			retVal.setTierData(writeUserTierData(factory, notesTier.getValue()));
		}
		return retVal;
	}

	private UserTierType writeUserTier(ObjectFactory factory, Tier<ca.phon.session.UserTierData> userTier) {
		final UserTierType retVal = factory.createUserTierType();
		if(userTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, userTier.getUnvalidatedValue()));
		} else {
			retVal.setTierData(writeUserTierData(factory, userTier.getValue()));
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
			NamespacePrefixMapper prefixMapper = new NamespacePrefixMapper() {
				@Override
				public String getPreferredPrefix(String namespace, String suggestion, boolean b) {
					if(namespace.equals("https://phon.ca/ns/session")) {
						return "phon";
					} else {
						return suggestion;
					}
				}
			};
			marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", prefixMapper);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
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
		return (args) -> { return new XmlSessionWriterV1_3(); };
	}

}

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
import ca.phon.session.usertier.*;
import ca.phon.util.Language;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.*;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
	private JAXBElement<XmlSessionType> toSessionType(Session session) throws IOException {
		final ObjectFactory factory = new ObjectFactory();
		final XmlSessionType retVal = factory.createXmlSessionType();

		// header data
		retVal.setVersion("1.3");
		retVal.setId(session.getName());
		retVal.setCorpus(session.getCorpus());

		if(session.getMediaLocation() != null && session.getMediaLocation().length() > 0) {
			retVal.setMedia(session.getMediaLocation());
		}
		final LocalDate date = session.getDate();
		if(date != null) {
			try {
				final DatatypeFactory df = DatatypeFactory.newInstance();
				final XMLGregorianCalendar cal = df.newXMLGregorianCalendar(
						GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault())));
				cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				retVal.setDate(cal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}

		final List<Language> langs = session.getLanguages();
		for(Language lang:langs) {
			retVal.getLanguages().add(lang.toString());
		}

		// participants
		final XmlParticipantsType parts = factory.createXmlParticipantsType();
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant part = session.getParticipant(i);
			final XmlParticipantType pt = writeParticipant(factory, part);
			parts.getParticipant().add(pt);
		}
		retVal.setParticipants(parts);

		// transcribers
		final XmlTranscribersType tt = factory.createXmlTranscribersType();
		for(int i = 0; i < session.getTranscriberCount(); i++) {
			final Transcriber tr = session.getTranscriber(i);
			final XmlTranscriberType trt = writeTranscriber(factory, tr);
			tt.getTranscriber().add(trt);
		}
		retVal.setTranscribers(tt);

		// tier info/ordering
		final XmlUserTiersType utt = factory.createXmlUserTiersType();
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			final XmlTierDescriptionType tierType = writeTierDescription(factory, td);
			utt.getTd().add(tierType);
		}
		retVal.setUserTiers(utt);

		final XmlTierOrderType tot = factory.createXmlTierOrderType();
		for(TierViewItem tvi:session.getTierView()) {
			final XmlTierViewType tvt = writeTierViewItem(factory, tvi);
			tot.getTierView().add(tvt);
		}
		retVal.setTierOrder(tot);

		final XmlTranscriptType transcript = factory.createXmlTranscriptType();
		retVal.setTranscript(transcript);
		// session data
		for(int i = 0; i < session.getTranscript().getNumberOfElements(); i++) {
			final var ele = session.getTranscript().getElementAt(i);
			if(ele.isComment()) {
				final Comment comment = ele.asComment();
				final XmlCommentType commentType = copyComment(factory, comment);
				transcript.getROrCommentOrGem().add(commentType);
			} else if(ele.isRecord()) {
				final XmlRecordType recordType = copyRecord(factory, retVal, ele.asRecord());
				transcript.getROrCommentOrGem().add(recordType);
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
			final XmlCommentType ct = copyComment(factory, com);
			transcript.getROrCommentOrGem().add(ct);
		}

		return factory.createSession(retVal);
	}

	private int pIdx = 0;
	/**
	 * copy participant info
	 */
	private XmlParticipantType writeParticipant(ObjectFactory factory, Participant part) {
		final XmlParticipantType retVal = factory.createXmlParticipantType();

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
			retVal.setSex(XmlSexType.MALE);
		else if(part.getSex() == Sex.FEMALE)
			retVal.setSex(XmlSexType.FEMALE);

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
	private XmlTranscriberType writeTranscriber(ObjectFactory factory, Transcriber tr) {
		final XmlTranscriberType retVal = factory.createXmlTranscriberType();

		retVal.setId(tr.getUsername());
		retVal.setName(tr.getRealName());
		final XmlPasswordType pst = factory.createXmlPasswordType();
		pst.setContent(tr.getPassword());
		pst.setUse(tr.usePassword());
		retVal.setPassword(pst);

		return retVal;
	}

	// tier descriptions
	private XmlTierDescriptionType writeTierDescription(ObjectFactory factory, TierDescription td) {
		final XmlTierDescriptionType retVal = factory.createXmlTierDescriptionType();

		final String name = td.getName();
		retVal.setTierName(name);

		XmlUserTierTypeType tierType = XmlUserTierTypeType.SIMPLE;
		if(td.getDeclaredType() == Orthography.class) {
			tierType = XmlUserTierTypeType.CHAT;
		} else if(td.getDeclaredType() == IPATranscript.class) {
			tierType = XmlUserTierTypeType.IPA;
		}
		retVal.setType(tierType);

		for(String key:td.getTierParameters().keySet()) {
			final XmlTierParameterType param = factory.createXmlTierParameterType();
			param.setName(key);
			param.setContent(td.getTierParameters().get(key));
			retVal.getTierParameters().getParam().add(param);
		}

		if(td.getTierAlignmentRules().getType() != ca.phon.session.TierAlignmentRules.TierAlignmentType.None) {
			final XmlTierAlignmentRules alignmentRules = factory.createXmlTierAlignmentRules();
			for(var type:td.getTierAlignmentRules().getWordAlignmentRules().getAlignableTypes()) {
				final XmlAlignableType at = switch(type) {
					case Freecode -> XmlAlignableType.FREECODE;
					case Group -> XmlAlignableType.GROUP;
					case InternalMedia -> XmlAlignableType.INTERNAL_MEDIA;
					case Linker -> XmlAlignableType.LINKER;
					case Pause -> XmlAlignableType.PAUSE;
					case PhoneticGroup -> XmlAlignableType.PHONETIC_GROUP;
					case Postcode -> XmlAlignableType.POSTCODE;
					case TagMarker -> XmlAlignableType.TAG_MARKER;
					case Terminator -> XmlAlignableType.TERMINATOR;
					case Word -> XmlAlignableType.WORD;
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

	private XmlTierViewType writeTierViewItem(ObjectFactory factory, TierViewItem tvi) {
		final boolean locked = tvi.isTierLocked();
		final boolean visible = tvi.isVisible();
		final String name = tvi.getTierName();
		final String font = tvi.getTierFont();

		final XmlTierViewType retVal = factory.createXmlTierViewType();
		retVal.setLocked(locked);
		retVal.setFont(font);
		retVal.setTierName(name);
		retVal.setVisible(visible);
		return retVal;
	}
	
	private XmlMediaType writeSegment(ObjectFactory factory, MediaSegment segment) {
		final XmlMediaType segType = factory.createXmlMediaType();
		segType.setStart(BigDecimal.valueOf(segment.getStartValue()));
		if(segment.getUnitType() == MediaUnit.Second)
			segType.getStart().setScale(3, RoundingMode.HALF_UP);
		segType.setEnd(BigDecimal.valueOf(segment.getEndValue()));
		if(segment.getUnitType() == MediaUnit.Second)
			segType.getEnd().setScale(3, RoundingMode.HALF_UP);
		final XmlMediaUnitType unitType = switch (segment.getUnitType()) {
			case Second -> XmlMediaUnitType.S;
			default -> XmlMediaUnitType.MS;
		};
		segType.setUnit(unitType);
		return segType;
	}

	// copy comment data
	private XmlCommentType copyComment(ObjectFactory factory, Comment com) {
		final XmlCommentType retVal = factory.createXmlCommentType();
		final XmlCommentTypeType ct = switch (com.getType()) {
			case Activities -> XmlCommentTypeType.ACTIVITIES;
			case Bck -> XmlCommentTypeType.BCK;
			case Bg -> XmlCommentTypeType.BEGIN_GEM;
			case Blank -> XmlCommentTypeType.BLANK;
			case Date -> XmlCommentTypeType.DATE;
			case Eg -> XmlCommentTypeType.END_GEM;
			case G -> XmlCommentTypeType.LAZY_GEM;
			case Generic -> XmlCommentTypeType.GENERIC;
			case Location -> XmlCommentTypeType.LOCATION;
			case NewEpisode -> XmlCommentTypeType.NEW_EPISODE;
			case Number -> XmlCommentTypeType.NUMBER;
			case Page -> XmlCommentTypeType.PAGE;
			case RecordingQuality -> XmlCommentTypeType.RECORDING_QUALITY;
			case RoomLayout -> XmlCommentTypeType.ROOM_LAYOUT;
			case Situation -> XmlCommentTypeType.SITUATION;
			case T -> XmlCommentTypeType.T;
			case TapeLocation -> XmlCommentTypeType.TAPE_LOCATION;
			case TimeDuration -> XmlCommentTypeType.TIME_DURATION;
			case TimeStart -> XmlCommentTypeType.TIME_START;
			case Transcriber -> XmlCommentTypeType.TRANSCRIBER;
			case Transcription -> XmlCommentTypeType.TRANSCRIPTION;
			case Types -> XmlCommentTypeType.TYPES;
			case Warning -> XmlCommentTypeType.WARNING;
		};
		retVal.setType(ct);

		if(com.getValue().getExtension(UnvalidatedValue.class) != null) {
			final UnvalidatedValue uv = com.getValue().getExtension(UnvalidatedValue.class);
			retVal.setUnparsed(writeUnparsed(factory, uv));
		} else {
			final XmlUserTierData tierData = writeUserTierData(factory, com.getValue());
			retVal.setTierData(tierData);
		}

		return retVal;
	}

	private XmlUnparsedData writeUnparsed(ObjectFactory factory, UnvalidatedValue unvalidatedValue) {
		final XmlUnparsedData retVal = factory.createXmlUnparsedData();
		retVal.setUnparsedValue(unvalidatedValue.getValue());
		final XmlParseErrorType pe = factory.createXmlParseErrorType();
		pe.setCharPositionInLine(unvalidatedValue.getParseError().getErrorOffset());
		pe.setContent(unvalidatedValue.getParseError().getMessage());
		retVal.setParseError(pe);
		return retVal;
	}

	private XmlUserTierData writeUserTierData(ObjectFactory factory, UserTierData tierData) {
		final XmlUserTierData retVal = factory.createXmlUserTierData();
		for(UserTierElement ele:tierData.getElements()) {
			if(ele instanceof TierString ts) {
				final XmlTierWordType tw = factory.createXmlTierWordType();
				tw.setContent(ts.toString());
				retVal.getTwOrTcOrInternalMedia().add(tw);
			} else if(ele instanceof UserTierComment tierComment) {
				final String commentText = tierComment.text();
				final XmlTierCommentType tc = factory.createXmlTierCommentType();
				tc.setContent(commentText);
				retVal.getTwOrTcOrInternalMedia().add(tc);
			} else if(ele instanceof UserTierInternalMedia im) {
				final XmlMediaType mediaType = factory.createXmlMediaType();
				mediaType.setStart(BigDecimal.valueOf(im.getInternalMedia().getStartTime()).setScale(3, RoundingMode.HALF_UP));
				mediaType.setEnd(BigDecimal.valueOf(im.getInternalMedia().getEndTime()).setScale(3, RoundingMode.HALF_UP));
				mediaType.setUnit(XmlMediaUnitType.S);
				retVal.getTwOrTcOrInternalMedia().add(mediaType);
			}
		}
		return retVal;
	}

	// record
	private XmlRecordType copyRecord(ObjectFactory factory, XmlSessionType session, Record record) {
		final XmlRecordType retVal = factory.createXmlRecordType();

		retVal.setExcludeFromSearches(record.isExcludeFromSearches());

		// orthography
		final Tier<Orthography> orthoTier = record.getOrthographyTier();
		final XmlOrthographyTierType orthoType = writeOrthographyTier(factory, orthoTier);
		retVal.setOrthography(orthoType);

		// ipa
		final Tier<IPATranscript> ipaTarget = record.getIPATargetTier();
		if(ipaTarget.hasValue()) {
			final XmlIpaTierType targetType = writeIPATier(factory, ipaTarget);
			retVal.setIpaTarget(targetType);
		}

		final Tier<IPATranscript> ipaActual = record.getIPAActualTier();
		if(ipaActual.hasValue()) {
			final XmlIpaTierType actualType = writeIPATier(factory, ipaActual);
			retVal.setIpaActual(actualType);
		}

		// notes
		final Tier<UserTierData> notesTier = record.getNotesTier();
		if(notesTier.hasValue()) {
			final XmlNotesTierType notesTierType = writeNotesTier(factory, notesTier);
			retVal.setNotes(notesTierType);
		}

		// segment
		final XmlMediaType segType = writeSegment(factory, record.getMediaSegment());
		retVal.setSegment(segType);

		// alignment
		final Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
		if(alignmentTier != null) {
			final XmlAlignmentTierType att = writeAlignmentTier(factory, record, alignmentTier);
			retVal.setAlignment(att);
		}

		for(String tierName:record.getUserDefinedTierNames()) {
			Tier<UserTierData> userTier = record.getTier(tierName, UserTierData.class);
			if(userTier != null && userTier.hasValue() && userTier.getValue().getElements().size() > 0) {
				final XmlUserTierType utt = writeUserTier(factory, userTier);
				utt.setName(userTier.getName());
				retVal.getUserTier().add(utt);
			}
		}

		return retVal;
	}

	private XmlOrthographyTierType writeOrthographyTier(ObjectFactory factory, Tier<Orthography> orthoTier) {
		final XmlOrthographyTierType retVal = factory.createXmlOrthographyTierType();
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
	private XmlUtteranceType writeOrthography(ObjectFactory factory, Orthography orthography) {
		final OrthoToXmlVisitor visitor = new OrthoToXmlVisitor();
		orthography.accept(visitor);
		return visitor.getU();
	}

	/**
	 *
	 * @param ipaTier
	 * @return {@link XmlIpaTierType}
	 */
	public XmlIpaTierType writeIPATier(ObjectFactory factory, Tier<IPATranscript> ipaTier) {
		final XmlIpaTierType retVal = factory.createXmlIpaTierType();
		if(ipaTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, ipaTier.getUnvalidatedValue()));
		} else {
			retVal.setPho(writeIPA(factory, ipaTier.getValue()));
		}

		final AlternativeTranscript alternativeTranscripts = ipaTier.getExtension(AlternativeTranscript.class);
		if(alternativeTranscripts != null) {
			for(String transcriber:alternativeTranscripts.keySet()) {
				final XmlBlindTranscriptionType btt = factory.createXmlBlindTranscriptionType();
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

	public XmlPhoneticTranscriptionType writeIPA(ObjectFactory factory, IPATranscript ipa) {
		final IpaToXmlVisitor visitor = new IpaToXmlVisitor();
		ipa.accept(visitor);
		return visitor.getPho();
	}

	/**
	 * Copy alignment data
	 */
	private XmlAlignmentTierType writeAlignmentTier(ObjectFactory factory, Record record, Tier<PhoneAlignment> alignmentTier) {
		final XmlAlignmentTierType retVal = factory.createXmlAlignmentTierType();
		int alignIdx = 0;
		for(PhoneMap pm:alignmentTier.getValue().getAlignments()) {
			int tidx = pm.getTopElements().length == 0 ? -1 : alignIdx;
			int aidx = pm.getBottomElements().length == 0 ? -1 : alignIdx;
			final XmlPhoneMapType pmType = writePhoneMap(factory, pm);
			pmType.setTarget(BigInteger.valueOf(tidx));
			pmType.setActual(BigInteger.valueOf(aidx));
			retVal.getPm().add(pmType);
			alignIdx++;
		}
		return retVal;
	}

	private XmlPhoneMapType writePhoneMap(ObjectFactory factory, PhoneMap pm) {
		final XmlPhoneMapType retVal = factory.createXmlPhoneMapType();
		retVal.getTop().addAll(List.of(pm.getTopAlignment()));
		retVal.getBottom().addAll(List.of(pm.getBottomAlignment()));
		return retVal;
	}

	private XmlNotesTierType writeNotesTier(ObjectFactory factory, Tier<UserTierData> notesTier) {
		final XmlNotesTierType retVal = factory.createXmlNotesTierType();
		if(notesTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, notesTier.getUnvalidatedValue()));
		} else {
			retVal.setTierData(writeUserTierData(factory, notesTier.getValue()));
		}
		return retVal;
	}

	private XmlUserTierType writeUserTier(ObjectFactory factory, Tier<UserTierData> userTier) {
		final XmlUserTierType retVal = factory.createXmlUserTierType();
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
		final JAXBElement<XmlSessionType> ele = toSessionType(session);
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
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

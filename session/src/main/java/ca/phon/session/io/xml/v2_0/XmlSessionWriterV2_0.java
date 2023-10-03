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
package ca.phon.session.io.xml.v2_0;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.mor.MorTierData;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionWriter;
import ca.phon.session.io.xml.v2_0.*;
import ca.phon.session.tierdata.*;
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
	namespace="https://phon.ca/ns/session",
	elementName="session",
	bindType=Session.class
)
@SessionIO(
		group="ca.phon",
		id="phonbank",
		version="2.0",
		mimetype="application/xml",
		extension="xml",
		name="Phon 4.0+ (.xml)"
)
@Rank(0)
public final class XmlSessionWriterV2_0 implements SessionWriter, IPluginExtensionPoint<SessionWriter> {

	public final static String DEFAULT_NAMESPACE = "https://phon.ca/ns/session";

	public final static String DEFAULT_NAMESPACE_LOCATION = "https://phon.ca/xml/xsd/session/v1_3/session.xsd";

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XmlSessionWriterV2_0.class.getName());

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
		retVal.setVersion("2.0");
		retVal.setName(session.getName());
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
		if(session.getParticipantCount() > 0)
			retVal.setParticipants(parts);

		// transcribers

		final XmlBlindModeType tt = factory.createXmlBlindModeType();
		for(int i = 0; i < session.getTranscriberCount(); i++) {
			final Transcriber tr = session.getTranscriber(i);
			final XmlTranscriberType trt = writeTranscriber(factory, tr);
			tt.getTranscriber().add(trt);
		}
		if(session.getTranscriberCount() > 0 || !session.getBlindTiers().isEmpty()) {
			tt.getBlindTier().clear();
			tt.getBlindTier().addAll(session.getBlindTiers());
			retVal.setBlindMode(tt);
		}

		// tier info/ordering
		final XmlUserTiersType utt = factory.createXmlUserTiersType();
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			final XmlTierDescriptionType tierType = writeTierDescription(factory, td);
			utt.getTd().add(tierType);
		}
		if(session.getUserTierCount() > 0)
			retVal.setUserTiers(utt);

		final XmlTierOrderType tot = factory.createXmlTierOrderType();
		for(TierViewItem tvi:session.getTierView()) {
			final XmlTierViewType tvt = writeTierViewItem(factory, tvi);
			tot.getTierView().add(tvt);
		}
		if(session.getTierView().size() > 0)
			retVal.setTierOrder(tot);

		final XmlTranscriptType transcript = factory.createXmlTranscriptType();
		retVal.setTranscript(transcript);
		// session data
		for(int i = 0; i < session.getTranscript().getNumberOfElements(); i++) {
			final var ele = session.getTranscript().getElementAt(i);
			if(ele.isComment()) {
				final Comment comment = ele.asComment();
				final XmlCommentType commentType = writeComment(factory, comment);
				transcript.getROrCommentOrGem().add(commentType);
			} else if(ele.isGem()) {
				final XmlGemType gemType = writeGem(factory, ele.asGem());
				transcript.getROrCommentOrGem().add(gemType);
			} else if(ele.isRecord()) {
				final XmlRecordType recordType = writeRecord(factory, retVal, ele.asRecord());
				transcript.getROrCommentOrGem().add(recordType);
			} else {
				// should not happen
			}
		}

		for(int tcIdx = 0; tcIdx < session.getMetadata().getNumberOfTrailingComments(); tcIdx++) {
			final Comment com = session.getMetadata().getTrailingComment(tcIdx);
			final XmlCommentType ct = writeComment(factory, com);
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

		if(part.getEducation() != null && part.getEducation().length() > 0)
			retVal.setEducation(part.getEducation());
		if(part.getGroup() != null && part.getGroup().length() > 0)
			retVal.setGroup(part.getGroup());

		final String lang = part.getLanguage();
		final String langs[] = (lang != null ? lang.split(" ") : new String[0]);
		for(String l:langs) {
			if(l.trim().length() > 0)
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
			prole = ParticipantRole.PARTICIPANT;
		XmlParticipantRoleType role = switch (prole) {
			case ADULT -> XmlParticipantRoleType.ADULT;
			case ATTORNEY -> XmlParticipantRoleType.ATTORNEY;
			case AUDIENCE -> XmlParticipantRoleType.AUDIENCE;
			case BOY -> XmlParticipantRoleType.BOY;
			case BROTHER -> XmlParticipantRoleType.BROTHER;
			case CARETAKER -> XmlParticipantRoleType.CARETAKER;
			case CHILD -> XmlParticipantRoleType.CHILD;
			case DOCTOR -> XmlParticipantRoleType.DOCTOR;
			case ENVIRONMENT -> XmlParticipantRoleType.ENVIRONMENT;
			case FATHER -> XmlParticipantRoleType.FATHER;
			case FEMALE -> XmlParticipantRoleType.FEMALE;
			case FRIEND -> XmlParticipantRoleType.FRIEND;
			case GIRL -> XmlParticipantRoleType.GIRL;
			case GRANDFATHER -> XmlParticipantRoleType.GRANDFATHER;
			case GRANDMOTHER -> XmlParticipantRoleType.GRANDMOTHER;
			case GROUP -> XmlParticipantRoleType.GROUP;
			case GUEST -> XmlParticipantRoleType.GUEST;
			case HOST -> XmlParticipantRoleType.HOST;
			case INFORMANT -> XmlParticipantRoleType.INFORMANT;
			case INVESTIGATOR -> XmlParticipantRoleType.INVESTIGATOR;
			case JUSTICE -> XmlParticipantRoleType.JUSTICE;
			case LEADER -> XmlParticipantRoleType.LEADER;
			case LENA -> XmlParticipantRoleType.LENA;
			case MALE -> XmlParticipantRoleType.MALE;
			case MEDIA -> XmlParticipantRoleType.MEDIA;
			case MEMBER -> XmlParticipantRoleType.MEMBER;
			case MOTHER -> XmlParticipantRoleType.MOTHER;
			case NARRATOR -> XmlParticipantRoleType.NARRATOR;
			case NURSE -> XmlParticipantRoleType.NURSE;
			case OTHER -> XmlParticipantRoleType.OTHER;
			case PARTICIPANT -> XmlParticipantRoleType.PARTICIPANT;
			case PARTNER -> XmlParticipantRoleType.PARTNER;
			case PLAYMATE -> XmlParticipantRoleType.PLAYMATE;
			case PLAYROLE -> XmlParticipantRoleType.PLAY_ROLE;
			case RELATIVE -> XmlParticipantRoleType.RELATIVE;
			case SIBLING -> XmlParticipantRoleType.SIBLING;
			case SISTER -> XmlParticipantRoleType.SISTER;
			case SPEAKER -> XmlParticipantRoleType.SPEAKER;
			case STUDENT -> XmlParticipantRoleType.STUDENT;
			case SUBJECT -> XmlParticipantRoleType.SUBJECT;
			case TARGET_ADULT -> XmlParticipantRoleType.TARGET_ADULT;
			case TARGET_CHILD -> XmlParticipantRoleType.TARGET_CHILD;
			case TEACHER -> XmlParticipantRoleType.TEACHER;
			case TEENAGER -> XmlParticipantRoleType.TEENAGER;
			case TEXT -> XmlParticipantRoleType.TEXT;
			case THERAPIST -> XmlParticipantRoleType.THERAPIST;
			case UNCERTAIN -> XmlParticipantRoleType.UNCERTAIN;
			case UNIDENTIFIED -> XmlParticipantRoleType.UNIDENTIFIED;
			case VISITOR -> XmlParticipantRoleType.VISITOR;
		};
		retVal.setRole(role);

		// create ID based on role if possible
		if(retVal.getId() == null && prole != null) {
			if(prole == ParticipantRole.TARGET_CHILD) {
				retVal.setId("CHI");
			} else if(prole == ParticipantRole.MOTHER) {
				retVal.setId("MOT");
			} else if(prole == ParticipantRole.FATHER) {
				retVal.setId("FAT");
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

		XmlUserTierTypeType tierType = XmlUserTierTypeType.DEFAULT;
		if(td.getDeclaredType() == Orthography.class) {
			tierType = XmlUserTierTypeType.CHAT;
		} else if(td.getDeclaredType() == IPATranscript.class) {
			tierType = XmlUserTierTypeType.IPA;
		} else if(td.getDeclaredType() == MorTierData.class) {
			tierType = XmlUserTierTypeType.MOR;
		}
		retVal.setType(tierType);

		for(String key:td.getTierParameters().keySet()) {
			final XmlTierParameterType param = factory.createXmlTierParameterType();
			param.setName(key);
			param.setContent(td.getTierParameters().get(key));
			retVal.getTierParameters().getParam().add(param);
		}

		retVal.setExcludeFromAlignment(td.isExcludeFromAlignment());

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
			segType.setStart(segType.getStart().setScale(3, RoundingMode.HALF_UP));
		segType.setEnd(BigDecimal.valueOf(segment.getEndValue()));
		if(segment.getUnitType() == MediaUnit.Second)
			segType.setEnd(segType.getEnd().setScale(3, RoundingMode.HALF_UP));
		final XmlMediaUnitType unitType = switch (segment.getUnitType()) {
			case Second -> XmlMediaUnitType.S;
			default -> XmlMediaUnitType.MS;
		};
		segType.setUnit(unitType);
		return segType;
	}

	private XmlGemType writeGem(ObjectFactory factory, Gem gem) {
		final XmlBeginEndLazyType type = switch (gem.getType()) {
			case End -> XmlBeginEndLazyType.END;
			case Lazy -> XmlBeginEndLazyType.LAZY;
			case Begin -> XmlBeginEndLazyType.BEGIN;
		};
		final XmlGemType retVal = factory.createXmlGemType();
		retVal.setType(type);
		retVal.setContent(gem.getLabel());
		return retVal;
	}

	// copy comment data
	private XmlCommentType writeComment(ObjectFactory factory, Comment com) {
		final XmlCommentType retVal = factory.createXmlCommentType();
		final XmlCommentTypeType ct = switch (com.getType()) {
			case Activities -> XmlCommentTypeType.ACTIVITIES;
			case Bck -> XmlCommentTypeType.BCK;
			case Blank -> XmlCommentTypeType.BLANK;
			case Date -> XmlCommentTypeType.DATE;
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
			final XmlTierData tierData = writeUserTierData(factory, com.getValue());
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

	private XmlTierData writeUserTierData(ObjectFactory factory, TierData tierData) {
		final XmlTierData retVal = factory.createXmlTierData();
		for(TierElement ele:tierData.getElements()) {
			if(ele instanceof TierString ts) {
				final XmlTierWordType tw = factory.createXmlTierWordType();
				tw.setContent(ts.toString());
				retVal.getTwOrTcOrInternalMedia().add(tw);
			} else if(ele instanceof TierComment tierComment) {
				final String commentText = tierComment.text();
				final XmlTierCommentType tc = factory.createXmlTierCommentType();
				tc.setContent(commentText);
				retVal.getTwOrTcOrInternalMedia().add(tc);
			} else if(ele instanceof TierInternalMedia im) {
				final XmlMediaType mediaType = factory.createXmlMediaType();
				mediaType.setStart(BigDecimal.valueOf(im.getInternalMedia().getStartTime()).setScale(3, RoundingMode.HALF_UP));
				mediaType.setEnd(BigDecimal.valueOf(im.getInternalMedia().getEndTime()).setScale(3, RoundingMode.HALF_UP));
				mediaType.setUnit(XmlMediaUnitType.S);
				retVal.getTwOrTcOrInternalMedia().add(mediaType);
			} else if(ele instanceof TierLink tl) {
				final XmlTierLinkType linkType = factory.createXmlTierLinkType();
				linkType.setHref(tl.getHref());
				linkType.setLabel(tl.getLabel());
				retVal.getTwOrTcOrInternalMedia().add(linkType);
			}
		}
		return retVal;
	}

	private XmlParticipantType findXmlParticipant(XmlSessionType sessionType, Participant participant) {
		if(sessionType.getParticipants() != null) {
			for (XmlParticipantType participantType : sessionType.getParticipants().getParticipant()) {
				if (participantType.getId().equals(participant.getId())) {
					return participantType;
				}
			}
		}
		return null;
	}

	// record
	private XmlRecordType writeRecord(ObjectFactory factory, XmlSessionType session, Record record) {
		final XmlRecordType retVal = factory.createXmlRecordType();

		retVal.setUuid(record.getUuid().toString());
		if(record.getSpeaker() != Participant.UNKNOWN)
			retVal.setSpeaker(findXmlParticipant(session, record.getSpeaker()));

		retVal.setExcludeFromSearches(record.isExcludeFromSearches());

		// orthography
		final Tier<Orthography> orthoTier = record.getOrthographyTier();
		final XmlOrthographyTierType orthoType = writeOrthographyTier(factory, orthoTier);
		retVal.setOrthography(orthoType);

		// ipa
		final Tier<IPATranscript> ipaTarget = record.getIPATargetTier();
		if(ipaTarget.hasValue() && ipaTarget.getValue().length() > 0) {
			final XmlIpaTierType targetType = writeIPATier(factory, ipaTarget);
			retVal.setIpaTarget(targetType);
		}

		final Tier<IPATranscript> ipaActual = record.getIPAActualTier();
		if(ipaActual.hasValue() && ipaActual.getValue().length() > 0) {
			final XmlIpaTierType actualType = writeIPATier(factory, ipaActual);
			retVal.setIpaActual(actualType);
		}

		// notes
		final Tier<TierData> notesTier = record.getNotesTier();
		if(notesTier.hasValue() && notesTier.getValue().length() > 0) {
			final XmlNotesTierType notesTierType = writeNotesTier(factory, notesTier);
			retVal.setNotes(notesTierType);
		}

		// segment
		final XmlMediaType segType = writeSegment(factory, record.getMediaSegment());
		retVal.setSegment(segType);

		// alignment
		final Tier<PhoneAlignment> alignmentTier = record.getPhoneAlignmentTier();
		if(alignmentTier != null && alignmentTier.getValue().getAlignments().size() > 0) {
			final XmlAlignmentTierType att = writeAlignmentTier(factory, record, alignmentTier);
			retVal.setAlignment(att);
		}

		for(String tierName:record.getUserDefinedTierNames()) {
			Tier<?> userTier = record.getTier(tierName);
			if(userTier != null && userTier.hasValue() && !userTier.getValue().toString().isEmpty()) {
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
			retVal.setU(writeOrthography(factory, orthoTier.getValue()));
		}

		if(orthoTier.isBlind()) {
			for(String transcriberId:orthoTier.getTranscribers()) {
				final Orthography blindOrtho = orthoTier.getBlindTranscription(transcriberId);
				final XmlBlindTranscriptionType xmlBlindTranscription = factory.createXmlBlindTranscriptionType();
				if(blindOrtho.getExtension(UnvalidatedValue.class) != null) {
					xmlBlindTranscription.setUnparsed(writeUnparsed(factory, blindOrtho.getExtension(UnvalidatedValue.class)));
				} else {
					xmlBlindTranscription.setU(writeOrthography(factory, blindOrtho));
				}
				retVal.getBlindTranscription().add(xmlBlindTranscription);
			}
		}

		return retVal;
	}

	/**
	 *
	 * @param factory
	 * @param orthography
	 * @return
	 */
	public XmlUtteranceType writeOrthography(ObjectFactory factory, Orthography orthography) {
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

		if(ipaTier.isBlind()) {
			for(String transcriberId:ipaTier.getTranscribers()) {
				final IPATranscript blindIpa = ipaTier.getBlindTranscription(transcriberId);
				final XmlBlindTranscriptionType xmlBlindTranscription = factory.createXmlBlindTranscriptionType();
				if(blindIpa.getExtension(UnvalidatedValue.class) != null) {
					xmlBlindTranscription.setUnparsed(writeUnparsed(factory, blindIpa.getExtension(UnvalidatedValue.class)));
				} else {
					xmlBlindTranscription.setPho(writeIPA(factory, blindIpa));
				}
				retVal.getBlindTranscription().add(xmlBlindTranscription);
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

	private XmlNotesTierType writeNotesTier(ObjectFactory factory, Tier<TierData> notesTier) {
		final XmlNotesTierType retVal = factory.createXmlNotesTierType();
		if(notesTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, notesTier.getUnvalidatedValue()));
		} else {
			retVal.setTierData(writeUserTierData(factory, notesTier.getValue()));
		}



		return retVal;
	}

	private XmlUserTierType writeUserTier(ObjectFactory factory, Tier<?> userTier) {
		final XmlUserTierType retVal = factory.createXmlUserTierType();
		final Class<?> tierType = userTier.getDeclaredType();
		if(userTier.isUnvalidated()) {
			retVal.setUnparsed(writeUnparsed(factory, userTier.getUnvalidatedValue()));
		} else {
			if(tierType == Orthography.class) {
				retVal.setU(writeOrthography(factory, (Orthography) userTier.getValue()));
			} else if(tierType == IPATranscript.class) {
				retVal.setPho(writeIPA(factory, (IPATranscript) userTier.getValue()));
			} else if(tierType == TierData.class) {
				retVal.setTierData(writeUserTierData(factory, (TierData) userTier.getValue()));
			} else {
				throw new IllegalArgumentException("Unsupported tier type " + tierType);
			}
		}

		if(userTier.isBlind()) {
			for(String transcriberId:userTier.getTranscribers()) {
				final XmlBlindTranscriptionType xmlBlindTranscription = factory.createXmlBlindTranscriptionType();
				Object blindVal = userTier.getBlindTranscription(transcriberId);
				if(blindVal instanceof Orthography blindOrtho) {
					xmlBlindTranscription.setU(writeOrthography(factory, blindOrtho));
				} else if(blindVal instanceof IPATranscript blindIpa) {
					xmlBlindTranscription.setPho(writeIPA(factory, blindIpa));
				} else if(blindVal instanceof TierData tierData) {
					xmlBlindTranscription.setTierData(writeUserTierData(factory, tierData));
				} else {
					throw new IllegalArgumentException("Unsupported blind tier type " + blindVal.getClass());
				}
				retVal.getBlindTranscription().add(xmlBlindTranscription);
			}
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
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, String.format("%s %s", DEFAULT_NAMESPACE, DEFAULT_NAMESPACE_LOCATION));
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
		return (args) -> { return new XmlSessionWriterV2_0(); };
	}

}

/*
 * Copyright (C) 2005-2023 Gregory Hedlund & Yvan Rose
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

import ca.phon.session.GemType;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.*;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.alignment.TierElementFilter;
import ca.phon.session.tierdata.*;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionReader;
import ca.phon.util.Language;
import ca.phon.util.Tuple;
import ca.phon.xml.XMLObjectReader;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.*;
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
	namespace="https://phon.ca/ns/session",
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
public final class XmlSessionReaderV1_3 implements SessionReader, XMLObjectReader<Session>, IPluginExtensionPoint<SessionReader> {

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
			final JAXBElement<XmlSessionType> XmlSessionTypeEle =
					unmarshaller.unmarshal(doc.getDocumentElement(), XmlSessionType.class);

			if(XmlSessionTypeEle != null && XmlSessionTypeEle.getValue() != null) {
				final XmlSessionType XmlSessionType = XmlSessionTypeEle.getValue();
				retVal = readXmlSessionType(XmlSessionType);
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		}

		return retVal;
	}

	/**
	 * Read session in from given xml {@link XmlSessionType} object.
	 *
	 * @param xmlSessionType
	 *
	 * @return session
	 */
	private Session readXmlSessionType(XmlSessionType xmlSessionType) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Session retVal = factory.createSession();

		// copy header info
		retVal.setName(xmlSessionType.getName());
		retVal.setCorpus(xmlSessionType.getCorpus());

		if(xmlSessionType.getMedia() != null && xmlSessionType.getMedia().length() > 0) {
			retVal.setMediaLocation(xmlSessionType.getMedia());
		}
		if(xmlSessionType.getDate() != null) {
			final XMLGregorianCalendar xmlDate = xmlSessionType.getDate();
			final LocalDate dateTime = LocalDate.of(
					xmlDate.getYear(),
					xmlDate.getMonth(),
					xmlDate.getDay());
			retVal.setDate(dateTime);
		}
		if(xmlSessionType.getLanguages().size() > 0) {
			List<Language> langs = new ArrayList<>();
			for(String lang:xmlSessionType.getLanguages()) {
				try {
					Language l = Language.parseLanguage(lang);
					langs.add(l);
				} catch (IllegalArgumentException e) {
					LOGGER.warn(e);
				}
			}
			retVal.setLanguages(langs);
		}

		// copy participant information
		final XmlParticipantsType participants = xmlSessionType.getParticipants();
		if(participants != null) {
			for(XmlParticipantType pt:participants.getParticipant()) {
				final Participant p = readParticipant(factory, pt, retVal.getDate());
				retVal.addParticipant(p);
			}
		}

		// copy tier information
		final XmlUserTiersType userTiers = xmlSessionType.getUserTiers();
		if(userTiers != null) {
			for(XmlTierDescriptionType tdt:userTiers.getTd()) {
				final TierDescription td = readTierDescription(factory, tdt);
				retVal.addUserTier(td);
			}

			// TODO
			for(XmlTierAlignmentRulesType xmlTierAlignmentRules:userTiers.getTierAlignmentRules()) {
				final Map<String, TierElementFilter> elementFilterMap = new LinkedHashMap<>();
				for (var xmlEleFilter : xmlTierAlignmentRules.getTierElementFilter()) {
					final Tuple<String, TierElementFilter> elementFilter = readTierElementFilter(xmlEleFilter);
					elementFilterMap.put(elementFilter.getObj1(), elementFilter.getObj2());
				}
			}
		}

		// copy transcriber information
		if(xmlSessionType.getBlindMode() != null) {
			retVal.setBlindTiers(xmlSessionType.getBlindMode().getBlindTier());
			for (XmlTranscriberType tt : xmlSessionType.getBlindMode().getTranscriber()) {
				final Transcriber t = copyTranscriber(factory, tt);
				retVal.addTranscriber(t);
			}
		}

		final List<TierViewItem> tierOrder = new ArrayList<TierViewItem>();
		if(xmlSessionType.getTierOrder() != null) {
			for (XmlTierViewType tot : xmlSessionType.getTierOrder().getTierView()) {
				final TierViewItem toi = readTierViewItem(factory, tot);
				tierOrder.add(toi);
			}
			retVal.setTierView(tierOrder);
		}

		// read transcript data
		if(xmlSessionType.getTranscript() != null) {
			for(Object uOrComment:xmlSessionType.getTranscript().getROrCommentOrGem()) {
				if(uOrComment instanceof XmlCommentType ct) {
					final Comment comment = readComment(factory, ct);
					retVal.getTranscript().addComment(comment);
				} else if(uOrComment instanceof XmlGemType gt) {
					final GemType type = switch (gt.getType()) {
						case BEGIN -> GemType.Begin;
						case END -> GemType.End;
						case LAZY -> GemType.Lazy;
					};
					retVal.getTranscript().addGem(factory.createGem(type, gt.getContent()));
				} else {
					final XmlRecordType rt = (XmlRecordType) uOrComment;
					Record record = null;
					try {
						record = factory.createRecord(new LazyRecord(factory, retVal, rt));
					} catch (Exception e) {
						LOGGER.info(rt.getUuid());
						LOGGER.error(
								e.getLocalizedMessage(), e);

					}
					retVal.addRecord(record);
				}
			}
		}

		return retVal;
	}

	Tuple<String, TierElementFilter> readTierElementFilter(Object obj) {
		return null;
	}

	// participants
	Participant readParticipant(SessionFactory factory, XmlParticipantType pt, LocalDate sessionDate) {
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

		if(pt.getSex() == XmlSexType.MALE)
			retVal.setSex(Sex.MALE);
		else if(pt.getSex() == XmlSexType.FEMALE)
			retVal.setSex(Sex.FEMALE);
		else
			retVal.setSex(Sex.UNSPECIFIED);

		ParticipantRole prole = switch (pt.getRole()) {
			case ADULT -> ParticipantRole.ADULT;
			case ATTORNEY -> ParticipantRole.ATTORNEY;
			case AUDIENCE -> ParticipantRole.AUDIENCE;
			case BOY -> ParticipantRole.BOY;
			case BROTHER -> ParticipantRole.BROTHER;
			case CARETAKER -> ParticipantRole.CARETAKER;
			case CHILD -> ParticipantRole.CHILD;
			case DOCTOR -> ParticipantRole.DOCTOR;
			case ENVIRONMENT -> ParticipantRole.ENVIRONMENT;
			case FATHER -> ParticipantRole.FATHER;
			case FEMALE -> ParticipantRole.FEMALE;
			case FRIEND -> ParticipantRole.FRIEND;
			case GIRL -> ParticipantRole.GIRL;
			case GRANDFATHER -> ParticipantRole.GRANDFATHER;
			case GRANDMOTHER -> ParticipantRole.GRANDMOTHER;
			case GROUP -> ParticipantRole.GROUP;
			case GUEST -> ParticipantRole.GUEST;
			case HOST -> ParticipantRole.HOST;
			case INFORMANT -> ParticipantRole.INFORMANT;
			case INVESTIGATOR -> ParticipantRole.INVESTIGATOR;
			case JUSTICE -> ParticipantRole.JUSTICE;
			case LEADER -> ParticipantRole.LEADER;
			case LENA -> ParticipantRole.LENA;
			case MALE -> ParticipantRole.MALE;
			case MEDIA -> ParticipantRole.MEDIA;
			case MEMBER -> ParticipantRole.MEMBER;
			case MOTHER -> ParticipantRole.MOTHER;
			case NARRATOR -> ParticipantRole.NARRATOR;
			case NURSE -> ParticipantRole.NURSE;
			case OTHER -> ParticipantRole.OTHER;
			case PARTICIPANT -> ParticipantRole.PARTICIPANT;
			case PARTNER -> ParticipantRole.PARTNER;
			case PLAYMATE -> ParticipantRole.PLAYMATE;
			case PLAY_ROLE -> ParticipantRole.PLAYROLE;
			case RELATIVE -> ParticipantRole.RELATIVE;
			case SIBLING -> ParticipantRole.SIBLING;
			case SISTER -> ParticipantRole.SISTER;
			case SPEAKER -> ParticipantRole.SPEAKER;
			case STUDENT -> ParticipantRole.STUDENT;
			case SUBJECT -> ParticipantRole.SUBJECT;
			case TARGET_ADULT -> ParticipantRole.TARGET_ADULT;
			case TARGET_CHILD -> ParticipantRole.TARGET_CHILD;
			case TEACHER -> ParticipantRole.TEACHER;
			case TEENAGER -> ParticipantRole.TEENAGER;
			case TEXT -> ParticipantRole.TEXT;
			case THERAPIST -> ParticipantRole.THERAPIST;
			case UNCERTAIN -> ParticipantRole.UNCERTAIN;
			case UNIDENTIFIED -> ParticipantRole.UNIDENTIFIED;
			case VISITOR -> ParticipantRole.VISITOR;
		};
		retVal.setRole(prole);
		retVal.setSES(pt.getSES());

		if(pt.getOther() != null) {
			retVal.setOther(pt.getOther());
		}

		return retVal;
	}

	// transcribers
	private Transcriber copyTranscriber(SessionFactory factory, XmlTranscriberType tt) {
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
	private TierDescription readTierDescription(SessionFactory factory, XmlTierDescriptionType tdt) {
		final String tierName = tdt.getTierName();
		final Class<?> tierType = switch (tdt.getType()) {
			case CHAT -> Orthography.class;
			case IPA -> IPATranscript.class;
			case PHONE_ALIGNMENT -> PhoneAlignment.class;
			case DEFAULT -> TierData.class;
		};
		final Map<String, String> tierParams = new LinkedHashMap<>();
		if(tdt.getTierParameters() != null) {
			for (XmlTierParameterType tp : tdt.getTierParameters().getParam()) {
				tierParams.put(tp.getName(), tp.getContent());
			}
		}
		return factory.createTierDescription(tierName, tierType, tierParams, tdt.isExcludeFromAlignment());
	}

	private TierViewItem readTierViewItem(SessionFactory factory, XmlTierViewType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();

		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	private MediaSegment readMediaSegment(SessionFactory factory, XmlMediaType st) {
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
	private Comment readComment(SessionFactory factory, XmlCommentType ct) {
		final CommentType type = switch (ct.getType()) {
			case ACTIVITIES -> CommentType.Activities;
			case BCK -> CommentType.Bck;
			case BLANK -> CommentType.Blank;
			case DATE -> CommentType.Date;
			case GENERIC -> CommentType.Generic;
			case LOCATION -> CommentType.Location;
			case NEW_EPISODE -> CommentType.NewEpisode;
			case NUMBER -> CommentType.Number;
			case PAGE -> CommentType.Page;
			case RECORDING_QUALITY -> CommentType.RecordingQuality;
			case ROOM_LAYOUT -> CommentType.RoomLayout;
			case SITUATION -> CommentType.Situation;
			case T -> CommentType.T;
			case TAPE_LOCATION -> CommentType.TapeLocation;
			case TIME_DURATION -> CommentType.TimeDuration;
			case TIME_START -> CommentType.TimeStart;
			case TRANSCRIBER -> CommentType.Transcriber;
			case TRANSCRIPTION -> CommentType.Transcription;
			case TYPES -> CommentType.Types;
			case WARNING -> CommentType.Warning;
		};
		final TierData tierData = readUserTierData(factory, ct.getTierData());
		return factory.createComment(type, tierData);
	}

	Record readRecord(SessionFactory factory, Session session, XmlRecordType rt) {
		final Record retVal = factory.createRecord();

		try {
			if(rt.getUuid() != null) {
				UUID uuid = UUID.fromString(rt.getUuid());
				retVal.setUuid(uuid);
			}
		} catch (IllegalArgumentException e) {
		}

		if(rt.getSpeaker() != null) {
			final XmlParticipantType pt = (XmlParticipantType) rt.getSpeaker();
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
		final XmlOrthographyTierType ot = rt.getOrthography();
		final Orthography orthography = readOrthography(factory, ot);
		retVal.setOrthography(orthography);
		for(var xmlBlindTranscription:rt.getOrthography().getBlindTranscription()) {
			final Orthography blindOrtho = readBlindOrthography(factory, xmlBlindTranscription);
			retVal.getOrthographyTier().setBlindTranscription(xmlBlindTranscription.getTranscriber(), blindOrtho);
		}

		// ipa target/actual
		if(rt.getIpaTarget() != null) {
			final IPATranscript ipaTarget = readTranscript(factory, rt.getIpaTarget());
			retVal.setIPATarget(ipaTarget);
			for(var xmlBlindTranscription:rt.getIpaTarget().getBlindTranscription()) {
				final IPATranscript blindIpa = readBlindIPATranscript(factory, xmlBlindTranscription);
				retVal.getIPATargetTier().setBlindTranscription(xmlBlindTranscription.getTranscriber(), blindIpa);
			}
		} else {
			retVal.setIPATarget(new IPATranscript());
		}

		if(rt.getIpaActual() != null) {
			final IPATranscript ipaActual = readTranscript(factory, rt.getIpaActual());
			retVal.setIPAActual(ipaActual);
			for(var xmlBlindTranscription:rt.getIpaActual().getBlindTranscription()) {
				final IPATranscript blindIpa = readBlindIPATranscript(factory, xmlBlindTranscription);
				retVal.getIPATargetTier().setBlindTranscription(xmlBlindTranscription.getTranscriber(), blindIpa);
			}
		} else {
			retVal.setIPAActual(new IPATranscript());
		}

		// notes
		if(rt.getNotes() != null) {
			final TierData notesData = readNotes(factory, rt.getNotes());
			retVal.setNotes(notesData);
			for(var xmlBlindTranscription:rt.getNotes().getBlindTranscription()) {
				final TierData blindNotes = readBlindUserTierTranscript(factory, xmlBlindTranscription);
				retVal.getNotesTier().setBlindTranscription(xmlBlindTranscription.getTranscriber(), blindNotes);
			}
		}

		// segment
		if(rt.getSegment() != null) {
			final MediaSegment segment = readMediaSegment(factory, rt.getSegment());
			retVal.setMediaSegment(segment);
		} else {
			retVal.setMediaSegment(factory.createMediaSegment());
		}

		// alignment
		if(rt.getAlignment() != null) {
			final PhoneAlignment alignment = readAlignment(factory, retVal, rt.getAlignment());
			retVal.setPhoneAlignment(alignment);
		}

		// user tiers
		for(XmlUserTierType utt:rt.getUserTier()) {
			final TierDescription td = findTierDescription(session, utt.getName());
			if(td == null) {
				throw new IllegalStateException("Invalid user tier " + utt.getName());
			}
			Tier<?> userTier = factory.createTier(utt.getName(), td.getDeclaredType(), td.getTierParameters(), td.isExcludeFromAlignment(), td.isBlind());
			if(td.getDeclaredType() == Orthography.class) {
				((Tier<Orthography>)userTier).setValue(readUserTierOrthography(factory, utt));
			} else if(td.getDeclaredType() == IPATranscript.class) {
				((Tier<IPATranscript>)userTier).setValue(readUserTierIPATranscript(factory, utt));
			} else if(td.getDeclaredType() == TierData.class) {
				((Tier<TierData>)userTier).setValue(readUserTier(factory, utt));
			} else {
				throw new IllegalArgumentException("Unsupported user tier type");
			}
			retVal.putTier(userTier);
		}

		return retVal;
	}

	private TierDescription findTierDescription(Session session, String tierName) {
		for(TierDescription td:session.getUserTiers()) {
			if(td.getName().equals(tierName))
				return td;
		}
		return null;
	}

	private UnvalidatedValue readParseError(XmlUnparsedData unparsableData) {
		final ParseException pe = new ParseException(unparsableData.getParseError().getContent(),
				unparsableData.getParseError().getCharPositionInLine());
		return new UnvalidatedValue(unparsableData.getUnparsedValue(), pe);
	}

	/**
	 * Read orthography tier in record
	 *
	 * @param factory
	 * @param ot
	 * @return
	 */
	private Orthography readOrthography(SessionFactory factory, XmlOrthographyTierType ot) {
		Orthography utt = new Orthography();
		if(ot.getU() != null) {
			utt = readOrthography(ot.getU());
		} else {
			utt.putExtension(UnvalidatedValue.class, readParseError(ot.getUnparsed()));
		}
		return utt;
	}

	public Orthography readOrthography(XmlUtteranceType ut) {
		final OrthographyBuilder builder = new OrthographyBuilder();
		final XmlOrthographyVisitor visitor = new XmlOrthographyVisitor(builder);
		if(ut.getLang() != null) {
			try {
				final Language lang = Language.parseLanguage(ut.getLang());
				builder.append(new UtteranceLanguage(lang));
			} catch (IllegalArgumentException e) {
				LOGGER.warn(e);
			}
		}
		ut.getLinker().forEach(visitor::visit);
		ut.getWOrGOrPg().forEach(visitor::visit);
		if(ut.getT() != null)
			visitor.visitTerminator(ut.getT());
		ut.getPostcode().forEach(visitor::visit);
		ut.getKOrError().forEach(visitor::visit);
		return builder.toOrthography();
	}

	/**
	 * Read ipa data.
	 *
	 * Attempts to copy phone objects as-is, if not possible
	 * the transcription is re-parsed.
	 *
	 * @param factory
	 * @param itt
	 */
	private IPATranscript readTranscript(SessionFactory factory, XmlIpaTierType itt) {
		IPATranscript retVal = new IPATranscript();
		if (itt.getPho() != null) {
			retVal = readTranscript(itt.getPho());
		} else if(itt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(itt.getUnparsed()));
		}
		return retVal;
	}

	private Orthography readUserTierOrthography(SessionFactory factory, XmlUserTierType utt) {
		Orthography retVal = new Orthography();
		if(utt.getU() != null) {
			retVal = readOrthography(utt.getU());
		} else if(utt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(utt.getUnparsed()));
		}
		return retVal;
	}

	private Orthography readBlindOrthography(SessionFactory factory, XmlBlindTranscriptionType itt) {
		Orthography retVal = new Orthography();
		if(itt.getU() != null) {
			retVal = readOrthography(itt.getU());
		} else if(itt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(itt.getUnparsed()));
		}
		return retVal;
	}

	private IPATranscript readUserTierIPATranscript(SessionFactory factory, XmlUserTierType utt) {
		IPATranscript retVal = new IPATranscript();
		if (utt.getPho() != null) {
			retVal = readTranscript(utt.getPho());
		} else {
			retVal.putExtension(UnvalidatedValue.class, readParseError(utt.getUnparsed()));
		}
		return retVal;
	}

	private IPATranscript readBlindIPATranscript(SessionFactory factory, XmlBlindTranscriptionType itt) {
		IPATranscript retVal = new IPATranscript();
		if (itt.getPho() != null) {
			retVal = readTranscript(itt.getPho());
		} else {
			retVal.putExtension(UnvalidatedValue.class, readParseError(itt.getUnparsed()));
		}
		return retVal;
	}

	private TierData readBlindUserTierTranscript(SessionFactory factory, XmlBlindTranscriptionType itt) {
		TierData retVal = new TierData();
		if(itt.getTierData() != null) {
			retVal = readUserTierData(factory, itt.getTierData());
		} else if(itt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(itt.getUnparsed()));
		}
		return retVal;
	}

	public IPATranscript readTranscript(XmlPhoneticTranscriptionType pho) {
		final XmlPhoneticTranscriptVisitor visitor = new XmlPhoneticTranscriptVisitor();
		pho.getPwOrPause().forEach(visitor::visit);
		try {
			return visitor.toIPATranscript();
		} catch (ParseException pe) {
			LOGGER.warn(pe);
			final IPATranscript retVal = new IPATranscript();
			retVal.putExtension(UnvalidatedValue.class, new UnvalidatedValue(visitor.toString(), pe));
			return retVal;
		}
	}

	/**
	 * Read alignment data
	 */
	private PhoneAlignment readAlignment(SessionFactory factory, Record record, XmlAlignmentTierType att) {
		final Tier<IPATranscript> ipaT = record.getIPATargetTier();
		final List<IPATranscript> targetWords = ipaT.hasValue() ? ipaT.getValue().words() : new ArrayList<>();
		final Tier<IPATranscript> ipaA = record.getIPAActualTier();
		final List<IPATranscript> actualWords = ipaA.hasValue() ? ipaA.getValue().words() : new ArrayList<>();

		final List<PhoneMap> alignments = new ArrayList<>();
		for(XmlPhoneMapType pmType:att.getPm()) {
			final int tidx = pmType.getTarget().intValue();
			final int aidx = pmType.getActual().intValue();
			final IPATranscript ipaTw = tidx >= 0 && tidx < targetWords.size() ? targetWords.get(tidx) : new IPATranscript();
			final IPATranscript ipaAw = aidx >= 0 && aidx < actualWords.size() ? actualWords.get(aidx) : new IPATranscript();
			final PhoneMap pm = new PhoneMap(ipaTw, ipaAw);

			final Integer[][] alignmentData = new Integer[2][];
			alignmentData[0] = pmType.getTop().toArray(new Integer[0]);
			alignmentData[1] = pmType.getBottom().toArray(new Integer[0]);
			if(alignmentData[0].length != alignmentData[1].length) {
				throw new IllegalStateException("Invalid alignment");
			}
			pm.setTopAlignment(alignmentData[0]);
			pm.setBottomAlignment(alignmentData[1]);
			alignments.add(pm);
		}

		return new PhoneAlignment(alignments);
	}

	private TierData readUserTier(SessionFactory factory, XmlUserTierType utt) {
		TierData retVal = new TierData();
		if(utt.getTierData() != null) {
			retVal = readUserTierData(factory, utt.getTierData());
		} else if(utt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(utt.getUnparsed()));
		}
		return retVal;
	}

	private TierData readNotes(SessionFactory factory, XmlNotesTierType ntt) {
		TierData retVal = new TierData();
		if(ntt.getTierData() != null) {
			retVal = readUserTierData(factory, ntt.getTierData());
		} else if(ntt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(ntt.getUnparsed()));
		}
		return retVal;
	}

	private TierData readUserTierData(SessionFactory factory, XmlTierData utd) {
		final List<TierElement> elements = new ArrayList<>();
		for(Object obj:utd.getTwOrTcOrInternalMedia()) {
			if(obj instanceof XmlTierWordType tierWordType) {
				elements.add(new TierString(tierWordType.getContent()));
			} else if(obj instanceof XmlTierCommentType tierCommentType) {
				elements.add(new TierComment(tierCommentType.getContent()));
			} else if(obj instanceof XmlMediaType mt) {
				final MediaSegment seg = readMediaSegment(factory, mt);
				elements.add(new TierInternalMedia(new InternalMedia(seg.getStartValue(), seg.getEndValue())));
			} else if(obj instanceof XmlTierLinkType tl) {
				final TierLink link = new TierLink(tl.getHref(), tl.getLabel());
				elements.add(link);
			} else {
				LOGGER.warn("Invalid element " + obj.toString());
			}
		}
		return new TierData(elements);
	}

	/**
	 * Get a dom version of the xml stream
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

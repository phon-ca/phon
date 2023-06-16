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
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.UserTierData;
import ca.phon.session.io.SessionIO;
import ca.phon.session.io.SessionReader;
import ca.phon.util.Language;
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
				final Participant p = readParticipant(factory, pt, retVal.getDate());
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
		for(TierViewType tot:sessionType.getTierOrder().getTv()) {
			final TierViewItem toi = readTierViewItem(factory, tot);
			tierOrder.add(toi);
		}
		retVal.setTierView(tierOrder);

		// read transcript data
		if(sessionType.getTranscript() != null) {
			for(Object uOrComment:sessionType.getTranscript().getROrComment()) {
				if(uOrComment instanceof CommentType) {
					final CommentType ct = (CommentType)uOrComment;
					final Comment comment = readComment(factory, ct);
					retVal.getTranscript().addComment(comment);
				} else {
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
				}
			}
		}

		return retVal;
	}

	// participants
	Participant readParticipant(SessionFactory factory, ParticipantType pt, LocalDate sessionDate) {
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
		final Class<?> tierType = switch (tdt.getType()) {
			case CHAT -> Orthography.class;
			case IPA -> IPATranscript.class;
			case SIMPLE -> UserTierData.class;
		};
		final Map<String, String> tierParams = new LinkedHashMap<>();
		if(tdt.getTierParameters() != null) {
			for (TierParameterType tp : tdt.getTierParameters().getParam()) {
				tierParams.put(tp.getName(), tp.getContent());
			}
		}
		ca.phon.session.TierAlignmentRules tierAlignmentRules = new ca.phon.session.TierAlignmentRules();
		if(tdt.getTierAlignment() != null) {
			final TierAlignmentRules alignmentRules = tdt.getTierAlignment();

			final List<TypeAlignmentRules.AlignableType> alignableTypes = new ArrayList<>();
			for(var alignableType:tdt.getTierAlignment().getAlignWith()) {
				final TypeAlignmentRules.AlignableType type = switch (alignableType) {
					case FREECODE -> TypeAlignmentRules.AlignableType.Freecode;
					case GROUP -> TypeAlignmentRules.AlignableType.Group;
					case INTERNAL_MEDIA -> TypeAlignmentRules.AlignableType.InternalMedia;
					case LINKER -> TypeAlignmentRules.AlignableType.Linker;
					case PAUSE -> TypeAlignmentRules.AlignableType.Pause;
					case PHONETIC_GROUP -> TypeAlignmentRules.AlignableType.PhoneticGroup;
					case POSTCODE -> TypeAlignmentRules.AlignableType.Postcode;
					case TAG_MARKER -> TypeAlignmentRules.AlignableType.TagMarker;
					case TERMINATOR -> TypeAlignmentRules.AlignableType.Terminator;
					case WORD -> TypeAlignmentRules.AlignableType.Word;
				};
				alignableTypes.add(type);
			}
			tierAlignmentRules = new ca.phon.session.TierAlignmentRules(
					new TypeAlignmentRules(alignableTypes, alignmentRules.isIncludeXXX(),
							alignmentRules.isIncludeYYY(), alignmentRules.isIncludeWWW(),
							alignmentRules.isIncludeOmitted(), alignmentRules.isIncludeExcluded()));
		}
		return factory.createTierDescription(tierName, tierType, tierParams, tierAlignmentRules);
	}

	private TierViewItem readTierViewItem(SessionFactory factory, TierViewType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();

		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	private MediaSegment readMediaSegment(SessionFactory factory, MediaType st) {
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
	private Comment readComment(SessionFactory factory, CommentType ct) {
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

	Record readRecord(SessionFactory factory, Session session, RecordType rt) {
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
		final OrthographyTierType ot = rt.getOrthography();
		final Orthography orthography = readOrthography(factory, ot);
		retVal.setOrthography(orthography);

		// ipa target/actual
		if(rt.getIpaTarget() != null) {
			final IPATranscript ipaTarget = readTranscript(factory, rt.getIpaTarget());
			final AlternativeTranscript blindTranscription = readBlindTranscriptions(factory, rt.getIpaTarget().getBlindTranscription());
			retVal.setIPATarget(ipaTarget);
			retVal.getIPATargetTier().putExtension(AlternativeTranscript.class, blindTranscription);
		} else {
			retVal.setIPATarget(new IPATranscript());
		}

		if(rt.getIpaActual() != null) {
			final IPATranscript ipaActual = readTranscript(factory, rt.getIpaActual());
			final AlternativeTranscript blindTranscription = readBlindTranscriptions(factory, rt.getIpaActual().getBlindTranscription());
			retVal.setIPAActual(ipaActual);
			retVal.getIPAActualTier().putExtension(AlternativeTranscript.class, blindTranscription);
		} else {
			retVal.setIPAActual(new IPATranscript());
		}

		// notes
		if(rt.getNotes() != null) {
			final UserTierData notesData = readNotes(factory, rt.getNotes());
			retVal.setNotes(notesData);
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
		for(UserTierType utt:rt.getUserTier()) {
			final UserTierData tierData = readUserTier(factory, utt);
			final TierDescription td = findTierDescription(session, utt.getName());
			if(td == null) {
				throw new IllegalStateException("Invalid user tier " + utt.getName());
			}
			final Tier<UserTierData> userTier = factory.createTier(utt.getName(), UserTierData.class,
					td.getTierAlignmentRules());
			userTier.setValue(tierData);
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

	private UnvalidatedValue readParseError(UnparsedData unparsableData) {
		final ParseException pe = new ParseException(unparsableData.getPe().getContent(),
				unparsableData.getPe().getCharPositionInLine());
		return new UnvalidatedValue(unparsableData.getUv(), pe);
	}

	/**
	 * Read orthography tier in record
	 *
	 * @param factory
	 * @param ot
	 * @return
	 */
	private Orthography readOrthography(SessionFactory factory, OrthographyTierType ot) {
		Orthography utt = new Orthography();
		if(ot.getU() != null) {
			utt = readOrthography(ot.getU());
		} else {
			utt.putExtension(UnvalidatedValue.class, readParseError(ot.getUnparsed()));
		}
		return utt;
	}

	private Orthography readOrthography(UtteranceType ut) {
		final OrthographyBuilder builder = new OrthographyBuilder();
		final XmlOrthographyVisitor visitor = new XmlOrthographyVisitor();
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
	private IPATranscript readTranscript(SessionFactory factory, IpaTierType itt) {
		IPATranscript retVal = new IPATranscript();
		if (itt.getPho() != null) {
			retVal = readTranscript(itt.getPho());
		} else {
			retVal.putExtension(UnvalidatedValue.class, readParseError(itt.getUnparsed()));
		}
		return retVal;
	}

	private IPATranscript readTranscript(PhoneticTranscriptionType pho) {
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

	private AlternativeTranscript readBlindTranscriptions(SessionFactory factory, List<BlindTranscriptionType> bts) {
		final AlternativeTranscript retVal = new AlternativeTranscript();
		for(BlindTranscriptionType btt:bts) {
			IPATranscript ipa = new IPATranscript();
			if (btt.getPho() != null) {
				ipa = readTranscript(btt.getPho());
			} else {
				ipa.putExtension(UnvalidatedValue.class, readParseError(btt.getUnparsed()));
			}
			retVal.put(btt.getTranscriber(), ipa);
		}
		return retVal;
	}

	/**
	 * Read alignment data
	 */
	private PhoneAlignment readAlignment(SessionFactory factory, Record record, AlignmentTierType att) {
		final Tier<IPATranscript> ipaT = record.getIPATargetTier();
		final List<IPATranscript> targetWords = ipaT.hasValue() ? ipaT.getValue().words() : new ArrayList<>();
		final Tier<IPATranscript> ipaA = record.getIPAActualTier();
		final List<IPATranscript> actualWords = ipaA.hasValue() ? ipaA.getValue().words() : new ArrayList<>();

		final List<PhoneMap> alignments = new ArrayList<>();
		for(PhoneMapType pmType:att.getPm()) {
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

	private UserTierData readUserTier(SessionFactory factory, UserTierType utt) {
		UserTierData retVal = new UserTierData();
		if(utt.getTierData() != null) {
			retVal = readUserTierData(factory, utt.getTierData());
		} else if(utt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(utt.getUnparsed()));
		}
		return retVal;
	}

	private UserTierData readNotes(SessionFactory factory, NotesTierType ntt) {
		UserTierData retVal = new UserTierData();
		if(ntt.getTierData() != null) {
			retVal = readUserTierData(factory, ntt.getTierData());
		} else if(ntt.getUnparsed() != null) {
			retVal.putExtension(UnvalidatedValue.class, readParseError(ntt.getUnparsed()));
		}
		return retVal;
	}

	private UserTierData readUserTierData(SessionFactory factory, ca.phon.session.io.xml.v1_3.UserTierData utd) {
		final List<UserTierElement> elements = new ArrayList<>();
		// all allowed objects are wrapped in JAXBElements
		for(Object obj:utd.getTwOrTcOrInternalMedia()) {
			if(!(obj instanceof JAXBElement<?>)) continue;
			final JAXBElement<?> ele = (JAXBElement<?>) obj;
			if(ele.getName().equals("tw")) {
				elements.add(new TierString(ele.getValue().toString()));
			} else if(ele.getName().equals("tc")) {
				elements.add(new UserTierComment(ele.getValue().toString()));
			} else if(ele.getName().equals("internal-media")) {
				final MediaType mt = (MediaType) ele.getValue();
				final MediaSegment seg = readMediaSegment(factory, mt);
				elements.add(new UserTierInternalMedia(new InternalMedia(seg.getStartValue(), seg.getEndValue())));
			} else {
				LOGGER.warn("Invalid element " + ele.getName());
			}
		}
		return new UserTierData(elements);
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

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
package ca.phon.session.io.xml.v1_2;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.*;
import ca.phon.orthography.Error;
import ca.phon.orthography.Linker;
import ca.phon.orthography.Pause;
import ca.phon.orthography.PauseLength;
import ca.phon.orthography.Word;
import ca.phon.plugin.*;
import ca.phon.session.Comment;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.*;
import ca.phon.session.io.xml.v12.*;
import ca.phon.session.io.xml.v12.CommentType;
import ca.phon.session.io.xml.v12.WordType;
import ca.phon.syllable.*;
import ca.phon.util.Language;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import ca.phon.xml.XMLObjectReader;
import ca.phon.xml.annotation.XMLSerial;
import jakarta.xml.bind.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.datatype.Duration;
import javax.xml.datatype.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.text.ParseException;
import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		name="Phon <3.6 (.xml)"
)
@Rank(1)
public class XmlSessionReaderV1_2 implements SessionReader, XMLObjectReader<Session>, IPluginExtensionPoint<SessionReader> {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(XmlSessionReaderV1_2.class.getName());

	@Override
	public Session read(Document doc, Element ele)
			throws IOException {
		Session retVal = null;

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
			if(headerData.getLanguage().size() > 0) {
				List<Language> langs = new ArrayList<>();
				for(String lang:headerData.getLanguage()) {
					final Language language = Language.parseLanguage(lang);
					langs.add(language);
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
		if(sessionType.getTranscript() != null) {
			for(Object uOrComment:sessionType.getTranscript().getUOrComment()) {
				if(uOrComment instanceof CommentType) {
					final CommentType ct = (CommentType)uOrComment;
					final Comment comment = copyComment(factory, ct);
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

		final Duration ageDuration = pt.getAge();
		if(ageDuration != null) {
			// convert to period
			final Period age = Period.of(ageDuration.getYears(), ageDuration.getMonths(), ageDuration.getDays());
			retVal.setAge(age);
		}

		retVal.setEducation(pt.getEducation());
		retVal.setGroup(pt.getGroup());

		String langs = "";
		for(String lang:pt.getLanguage())
			langs += (langs.length() > 0 ? " " : "") + lang;
		retVal.setLanguage(langs);

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
		final String name = utt.getTierName();
		
		try {
			Class<?> type = Class.forName(utt.getType(), true, PluginManager.getInstance());
			return factory.createTierDescription(name, type, new HashMap<>(), new TierAlignmentRules(new TypeAlignmentRules()));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private TierViewItem copyTierViewItem(SessionFactory factory, TvType tvt) {
		final boolean locked = tvt.isLocked();
		final boolean visible = tvt.isVisible();
		final String name = tvt.getTierName();
		final String font = tvt.getFont();

		return factory.createTierViewItem(name, visible, font, locked);
	}
	
	private MediaSegment copySegment(SessionFactory factory, SegmentType st) {
		final MediaSegment segment = factory.createMediaSegment();
		segment.setStartValue(Math.abs(st.getStartTime()));
		segment.setEndValue(Math.abs(st.getStartTime()) + Math.abs(st.getDuration()));
		segment.setUnitType(MediaUnit.Millisecond);
		return segment;
	}

	// copy comment data
	private Comment copyComment(SessionFactory factory, CommentType ct) {
		final String tag = ct.getType();
		ca.phon.session.CommentType type = ca.phon.session.CommentType.fromString(tag);
		if(type == null) {
			type = ca.phon.session.CommentType.Generic;
		}
		final StringBuffer buffer = new StringBuffer();
		for(Object obj:ct.getContent()) {
//			if(obj instanceof JAXBElement) {
//				final JAXBElement<?> jaxbEle = (JAXBElement<?>)obj;
//				if(jaxbEle.getDeclaredType() == SegmentType.class)
//					segment = copySegment(factory, (SegmentType)jaxbEle.getValue());
//			} else {
				buffer.append(obj.toString());
//			}
		}
		return factory.createComment(tag, buffer.toString(), null);
	}

	Record copyRecord(SessionFactory factory, Session session, RecordType rt) {
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
		final OrthographyType ot = rt.getOrthography();
		final Orthography orthography = copyOrthography(factory, ot);
		retVal.setOrthography(orthography);

		// ipa target/actual
		for(IpaTierType ipaTt:rt.getIpaTier()) {
			final SystemTierType tierType =
					(ipaTt.getForm() == PhoTypeType.MODEL ? SystemTierType.IPATarget : SystemTierType.IPAActual);
			final IPATranscript ipaTranscript = copyTranscript(factory, ipaTt);
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
					(btt.getForm() == PhoTypeType.MODEL ? retVal.getIPATargetTier() : retVal.getIPAActualTier());
			int gidx = 0;
			for(BgType bgt:btt.getBg()) {
				final StringBuffer buffer = new StringBuffer();
				for(WordType wt:bgt.getW()) {
					if(buffer.length() > 0)
						buffer.append(" ");
					buffer.append(wt.getContent());
				}

				final IPATranscript ipa = ipaTier.getValue();
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
		if(rt.getNotes() != null) {
			try {
				final UserTierData notesData = UserTierData.parseTierData(rt.getNotes().getContent());
				retVal.setNotes(notesData);
			} catch (ParseException pe) {
				final UserTierData userTierData = new UserTierData();
				userTierData.putExtension(UnvalidatedValue.class, new UnvalidatedValue(rt.getNotes().getContent(), pe));
				retVal.setNotes(userTierData);
			}
		}

		// segment
		if(rt.getSegment() != null) {
			final MediaSegment segment = copySegment(factory, rt.getSegment());
			retVal.setMediaSegment(segment);
		} else {
			retVal.setMediaSegment(factory.createMediaSegment());
		}

		// alignment
		for(AlignmentTierType att:rt.getAlignment()) {
			final PhoneMap alignment = copyAlignment(factory, retVal, att);
			retVal.setPhoneAlignment(alignment);
			break; // only processing the first alignment element (which should be the only one)
		}

		// user tiers
		for(FlatTierType ftt:rt.getFlatTier()) {
			final TierDescription td = session.getUserTier(ftt.getTierName());
			if(td == null) {
				throw new IllegalStateException("User tier not found in session " + ftt.getTierName());
			}
			final Tier<UserTierData> userTier = factory.createTier(ftt.getTierName());
			userTier.setText(ftt.getContent());
			retVal.putTier(userTier);
		}

		for(GroupTierType gtt:rt.getGroupTier()) {
			final TierDescription td = session.getUserTier(gtt.getTierName());
			if(td == null) {
				throw new IllegalStateException("User tier not found in session " + gtt.getTierName());
			}
			final Tier<UserTierData> userTier = factory.createTier(gtt.getTierName(), UserTierData.class,
					new TierAlignmentRules(new TypeAlignmentRules()));
			final StringBuffer buffer = new StringBuffer();
			for(TgType tgt:gtt.getTg()) {
				if(buffer.length() > 0)
					buffer.append(" ");
				for(WordType wt:tgt.getW()) {
					if(buffer.length() > 0)
						buffer.append(" ");
					buffer.append(wt.getContent());
				}
			}
			userTier.setText(buffer.toString());
			retVal.putTier(userTier);
		}

		return retVal;
	}

	/**
	 * Copy orthography
	 *
	 * @param factory
	 * @param ot
	 * @return orthography
	 */
	private Orthography copyOrthography(SessionFactory factory, OrthographyType ot) {
		final OrthographyBuilder builder = new OrthographyBuilder();

		// insert commas unless we have the situation of one word in each group
		boolean allOnes = true;
		for(Object otEle: ot.getWOrGOrP()) {
			if (!(otEle instanceof GroupType gt)) continue;
			allOnes &= gt.getWOrComOrE().size() == 1;
		}
		final boolean insertCommas = !allOnes;

		for(Object otEle:ot.getWOrGOrP()) {
			if(!(otEle instanceof GroupType gt)) continue;
			if(insertCommas && builder.size() > 0) {
				builder.append(new TagMarker(TagMarkerType.COMMA));
			}
			for(Object ele:gt.getWOrComOrE()) {
				if(ele instanceof WordType) {
					final WordType wt = (WordType)ele;
					builder.append(new ca.phon.orthography.Word(new WordText(wt.getContent())));
				} else if(ele instanceof EventType) {
					final EventType et = (EventType) ele;
					final String txt = et.getContent();
					builder.append(new Happening(txt));
				} else if(ele instanceof CommentType) {
					final CommentType ct = (CommentType) ele;
					final String tag = ct.getType();
					final StringBuilder sb = new StringBuilder();
					ct.getContent().forEach(sb::append);
					final String txt = sb.toString();

					if(tag == null || tag.length() == 0) {
						final LinkerType lt = LinkerType.fromString(txt);
						if (lt != null) {
							builder.append(new Linker(lt));
							continue;
						}

						final MarkerType mt = MarkerType.fromString(txt);
						if (mt != null) {
							builder.append(new Marker(mt));
							continue;
						}

						final TagMarkerType tagMarkerType = TagMarkerType.fromString(txt);
						if (tagMarkerType != null) {
							builder.append(new TagMarker(tagMarkerType));
							continue;
						}

						final TerminatorType tt = TerminatorType.fromString(txt);
						if (tt != null) {
							builder.append(new Terminator(tt));
							continue;
						}

						if ("^c".equals(txt)) {
							builder.append(new Separator(SeparatorType.CLAUSE_DELIMITER));
							continue;
						}

						final String overlapPtRegex = "([⌈⌉⌊⌋])([0-9]+)?";
						if (txt.matches(overlapPtRegex)) {
							final Pattern pattern = Pattern.compile(overlapPtRegex);
							final Matcher matcher = pattern.matcher(txt);
							if (matcher.matches()) {
								final String type = matcher.group(1);
								final OverlapPointType overlapPointType = switch (type) {
									case "⌈" -> OverlapPointType.TOP_START;
									case "⌉" -> OverlapPointType.TOP_END;
									case "⌊" -> OverlapPointType.BOTTOM_START;
									case "⌋" -> OverlapPointType.BOTTOM_END;
									// should never happen
									default -> null;
								};
								final int index = (matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : -1);
								builder.append(new OverlapPoint(overlapPointType, index));
							}
							continue;
						}

						// pause
						if (txt.matches("\\.{1,3}")) {
							if (txt.length() == 1)
								builder.append(new Pause(PauseLength.SIMPLE));
							else if (txt.length() == 2)
								builder.append(new Pause(PauseLength.LONG));
							else if (txt.length() == 3)
								builder.append(new Pause(PauseLength.VERY_LONG));
							continue;
						}

						//  overlap
						if (txt.matches("[<>][0-9]*")) {
							final OverlapType type = (txt.charAt(0) == '<' ? OverlapType.OVERLAP_PRECEEDS : OverlapType.OVERLAP_FOLLOWS);
							final String remainder = (txt.length() > 1 ? txt.substring(1) : null);
							int index = (remainder == null ? -1 : Integer.parseInt(remainder));
							builder.append(new Overlap(type, index));
							continue;
						}

						// replacement
						if (txt.matches("::? .+")) {
							final String type = txt.substring(0, txt.indexOf(' '));
							final String data = txt.substring(txt.indexOf(' ') + 1);
							final boolean real = type.length() == 2;
							// wrap everything in a single WordText object, the parser will fix at the end
							builder.append(new Replacement(real, new Word(new WordText(data))));
							continue;
						}

						// duration
						final String durationRegex = "# ([0-9.]+)(m?s)?";
						if (txt.matches(durationRegex)) {
							final Pattern pattern = Pattern.compile(durationRegex);
							final Matcher matcher = pattern.matcher(txt);
							if (matcher.matches()) {
								Float length = Float.parseFloat(matcher.group(1));
								final String unit = matcher.group(2);
								if (unit != null && "ms".equals(unit)) {
									length *= 1000.0f;
								}
								builder.append(new ca.phon.orthography.Duration(length));
							}
							continue;
						}

						if (txt.startsWith("^ ")) {
							builder.append(new Freecode(txt.substring(2)));
							continue;
						} else if (txt.startsWith("=")) {
							builder.append(new GroupAnnotation(GroupAnnotationType.EXPLANATION, txt.substring(1)));
							continue;
						} else if (txt.startsWith("=!")) {
							builder.append(new GroupAnnotation(GroupAnnotationType.PARALINGUISTICS, txt.substring(2)));
							continue;
						} else if (txt.startsWith("=?")) {
							builder.append(new GroupAnnotation(GroupAnnotationType.ALTERNATIVE, txt.substring(2)));
							continue;
						}

						// finally add as comments
						builder.append(new GroupAnnotation(GroupAnnotationType.COMMENTS, txt));
					} else if("happening".equals(tag)) {
						builder.append(new Happening(txt));
					} else if("error".equals(tag)) {
						builder.append(new Error(txt));
					} else if("long-feature-begin".equals(tag)) {
						builder.append(new LongFeature(BeginEnd.BEGIN, txt));
					} else if("long-feature-end".equals(tag)) {
						builder.append(new LongFeature(BeginEnd.END, txt));
					} else if("nonvocal".equals(tag)) {
						builder.append(new Nonvocal(BeginEndSimple.SIMPLE, txt));
					} else if("nonvocal-begin".equals(tag)) {
						builder.append(new Nonvocal(BeginEndSimple.BEGIN, txt));
					} else if("nonvocal-end".equals(tag)) {
						builder.append(new Nonvocal(BeginEndSimple.END, txt));
					} else if("action".equals(tag)) {
						builder.append(new Action());
					} else if("freecode".equals(tag)) {
						builder.append(new Freecode(txt));
					}
				} else if(ele instanceof InnerGroupMarker) {
					// not used
				} else if(ele instanceof PunctuationType) {
					final PunctuationType pt = (PunctuationType) ele;
					switch(pt.getType()) {
						case "PERIOD" -> builder.append(new Terminator(TerminatorType.PERIOD));
						case "COMMA" -> builder.append(new TagMarker(TagMarkerType.COMMA));
					}
				}
			}
		}

		final String orthoTxt = builder.toOrthography().toString();
		try {
			Orthography retVal = Orthography.parseOrthography(orthoTxt);
			return retVal;
		} catch (ParseException pe) {
			final Orthography retVal = new Orthography();
			retVal.putExtension(UnvalidatedValue.class, new UnvalidatedValue(orthoTxt, pe));
			return retVal;
		}
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
	private IPATranscript copyTranscript(SessionFactory factory, IpaTierType itt) {
		final IPATranscriptBuilder builder = new IPATranscriptBuilder();
		// attempt an exact copy first
		for(PhoType pt:itt.getPg()) {
			if(pt != null && pt.getW() != null) {
				for(WordType wt:pt.getW()) {
					if(builder.size() > 0)
						builder.appendWordBoundary();
					builder.append(wt.getContent());
				}
			}
		}
		return builder.toIPATranscript();
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
	private PhoneMap copyAlignment(SessionFactory factory, Record record, AlignmentTierType att) {
		final IPATranscript ipaT = record.getIPATargetTier().hasValue() ? record.getIPATarget() : new IPATranscript();
		final IPATranscript ipaA = record.getIPAActualTier().hasValue() ? record.getIPAActual() : new IPATranscript();
		int alignLength = 0;
		for(AlignmentType at:att.getAg()) {
			alignLength += at.getLength();
		}
		int alignIdx = 0;
		final PhoneMap pm = new PhoneMap(ipaT, ipaA);
		final Integer[][] alignmentData = new Integer[2][];
		alignmentData[0] = new Integer[alignLength];
		alignmentData[1] = new Integer[alignLength];
		for(AlignmentType at:att.getAg()) {
			for(int i = 0; i < at.getPhomap().size(); i++) {
				final MappingType mt = at.getPhomap().get(i);
				alignmentData[0][alignIdx] =
						(mt.getValue().size() > 0 ? mt.getValue().get(0) : null);
				alignmentData[1][alignIdx] =
						(mt.getValue().size() > 1 ? mt.getValue().get(1) : null);
				++alignIdx;
			}
		}
		pm.setTopAlignment(alignmentData[0]);
		pm.setBottomAlignment(alignmentData[1]);
		return pm;
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
					&& evt.asStartElement().getAttributeByName(new QName("version")).getValue().equals("PB1.2");
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
		return (args) -> { return new XmlSessionReaderV1_2(); };
	}

}

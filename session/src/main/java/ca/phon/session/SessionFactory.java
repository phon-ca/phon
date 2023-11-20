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
package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.plugin.*;
import ca.phon.session.impl.GemImpl;
import ca.phon.session.io.*;
import ca.phon.session.spi.*;
import ca.phon.session.tierdata.TierData;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.util.*;

/**
 * A factory for creating mutable session objects.
 * 
 */
public final class SessionFactory extends ExtendableObject {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionFactory.class.getName());
	
	private final SessionFactorySPI sessionFactoryImpl;
	
	/**
	 * Create a new session factory.
	 * 
	 * @return factory using first available
	 *  implementation. <code>null</code> if no
	 *  implementation found.
	 */
	public static SessionFactory newFactory() {
		final PluginManager manager = PluginManager.getInstance();
		final List<IPluginExtensionPoint<SessionFactorySPI>> sessionFactoryExtPts = 
				manager.getExtensionPoints(SessionFactorySPI.class);
		
		if(sessionFactoryExtPts.size() > 0) {
			return new SessionFactory(sessionFactoryExtPts.get(0).getFactory().createObject(new Object[0]));
		} else {
			throw new IllegalStateException("No session factory available");
		}
	}

	private SessionFactory(SessionFactorySPI impl) {
		super();
		this.sessionFactoryImpl = impl;
	}

	/**
	 * Create a session path object
	 *
	 * @param path of sesion as corpus/session
	 * @return session path object
	 */
	public SessionPath createSessionPath(String path) {
		return new SessionPath(path);
	}

	/**
	 * Create new session path
	 *
	 * @param corpus name starting from project location
	 * @param session name including extension
	 * @return new session path
	 */
	public SessionPath createSessionPath(String corpus, String session) {
		return new SessionPath(corpus, session);
	}

	/**
	 * Create a new empty session.
	 * Tier view 
	 * 
	 * @return a new session object
	 */
	public Session createSession() {
		SessionSPI sessionImpl = sessionFactoryImpl.createSession();
		return createSession(sessionImpl);
	}
	
	public Session createSession(SessionSPI sessionImpl) {
		return new Session(sessionImpl);
	}
	
	/**
	 * Create a new session with the specified
	 * corpus and name. Also sets up initial tier view.
	 * 
	 * @param corpus
	 * @param name
	 * 
	 * @return the session object
	 */
	public Session createSession(String corpus, String name) {
		final Session retVal = createSession();
		retVal.setCorpus(corpus);
		retVal.setName(name);
		setupDefaultTierView(retVal);
		return retVal;
	}

	public Transcript createTranscript() {
		return new Transcript(sessionFactoryImpl.createTranscript());
	}
	
	/**
	 * Setup default tier view for session.  As of Phon 4.0 Notes and Segment are not added to the default
	 * tier view.
	 * 
	 * @param session
	 */
	public void setupDefaultTierView(Session session) {
		final List<TierViewItem> tierView = new ArrayList<TierViewItem>();
		tierView.add(createTierViewItem(SystemTierType.Orthography.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.IPATarget.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.IPAActual.getName(), true));
		for(TierDescription tierDesc:session.getUserTiers()) {
			tierView.add(createTierViewItem(tierDesc.getName(), true, "default", false));
		}
		session.setTierView(tierView);
	}
	
	/**
	 * Clone given session.
	 * 
	 * @param session
	 * 
	 * @return the cloned session
	 */
	public Session cloneSession(Session session) {
		final Session retVal = createSession();
		
		copySessionInformation(session, retVal);
		for(Participant part:session.getParticipants()) {
			final Participant clonedPart = cloneParticipant(part);
			retVal.addParticipant(clonedPart);
		}
		
		for(Record r:session.getRecords()) {
			final Record clonedRecord = cloneRecord(r);
			retVal.addRecord(clonedRecord);
		}
		
		return retVal;
	}
	
	/**
	 * Copy session information from one session to the
	 * destination.  Session information includes:
	 * <ul><li>media location</li>
	 * <li>session name</li>
	 * <li>corpus name</li>
	 * </ul>
	 * 
	 * @param session
	 * @param dest
	 */
	public void copySessionInformation(Session session, Session dest) {
		dest.setName(session.getName());
		dest.setCorpus(session.getCorpus());
		dest.setMediaLocation(session.getMediaLocation());
		dest.setDate(session.getDate());
		dest.setLanguages(session.getLanguages());
	}
	
	/**
	 * Create empty generic comment
	 * 
	 * @return empty generic comment
	 */
	public Comment createComment() {
		return createComment(CommentType.Generic);
	}

	/**
	 * Create generic comment with value
	 *
	 * @param value
	 * @return comment
	 */
	public Comment createComment(TierData value) {
		return createComment(CommentType.Generic, value);
	}

	/**
	 * Create new empty comment of given type
	 *
	 * @param commentType
	 * @return comment
	 */
	public Comment createComment(CommentType commentType) {
		return createComment(commentType, new TierData());
	}

	/**
	 * Create comment with type and value
	 *
	 * @param commentType
	 * @param value
	 *
	 * @return new comment
	 */
	public Comment createComment(CommentType commentType, TierData value) {
		return createComment(sessionFactoryImpl.createComment(commentType, value));
	}

	public Comment createComment(CommentSPI commentImpl) {
		return new Comment(commentImpl);
	}

	public Comment cloneComment(Comment comment) {
		final Comment retVal = createComment();
		retVal.setType(comment.getType());
		retVal.setValue(comment.getValue());
		return retVal;
	}

	/**
	 * Create empty lazy gem
	 *
	 * @return empty lazy gem
	 */
	public Gem createGem() {
		return createGem("");
	}

	/**
	 * Create lazy gem with label
	 *
	 * @param label
	 * @return label gem with label
	 */
	public Gem createGem(String label) {
		return createGem(GemType.Lazy, label);
	}

	/**
	 * Create gem of given type with label
	 *
	 * @param gemType
	 * @param label
	 * @return gem
	 */
	public Gem createGem(GemType gemType, String label) {
		return createGem(new GemImpl(gemType, label));
	}

	public Gem createGem(GemSPI spi) {
		return new Gem(spi);
	}

	public Transcript createSessionTranscript() {
		return null;
	}

	/**
	 * Copy tier information from one session to another.
	 * 
	 * @param session
	 * @param dest
	 */
	public void copySessionTierInformation(Session session, Session dest) {
		for(TierDescription tierDesc:session.getUserTiers()) {
			final TierDescription tierCopy =
					createTierDescription(tierDesc.getName(), tierDesc.getDeclaredType(), tierDesc.getTierParameters(), tierDesc.isExcludeFromAlignment(), tierDesc.isBlind(), tierDesc.getSubtypeDelim(), tierDesc.getSubtypeExpr());
			dest.addUserTier(tierCopy);
		}
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newTierView = new ArrayList<>();
		for(TierViewItem tvi:tierView) {
			final TierViewItem tierViewCopy =
					createTierViewItem(tvi.getTierName(), tvi.isVisible(), tvi.getTierFont(), tvi.isTierLocked());
			newTierView.add(tierViewCopy);
		}
		dest.setTierView(newTierView);
	}
	
	/**
	 * Create a new record.
	 * 
	 * @return a new empty record
	 */
	public Record createRecord() {
		final RecordSPI recordImpl = sessionFactoryImpl.createRecord();
		return createRecord(recordImpl);
	}

	/**
	 * Create a new record for the given session with current blind tier status and tier params setup
	 * correctly for system tiers.
	 *
	 * @param session
	 * @return new record based on session config
	 */
	public Record createRecord(Session session) {
		// setup system tier param map
		final Map<SystemTierType, Map<String, String>> systemTierParamMap = new LinkedHashMap<>();
		systemTierParamMap.put(SystemTierType.Orthography, session.getSystemTierParameters(SystemTierType.Orthography));
		systemTierParamMap.put(SystemTierType.IPATarget, session.getSystemTierParameters(SystemTierType.IPATarget));
		systemTierParamMap.put(SystemTierType.IPAActual, session.getSystemTierParameters(SystemTierType.IPAActual));
		systemTierParamMap.put(SystemTierType.PhoneAlignment, session.getSystemTierParameters(SystemTierType.PhoneAlignment));
		systemTierParamMap.put(SystemTierType.Notes, session.getSystemTierParameters(SystemTierType.Notes));

		// blind tiers
		final List<SystemTierType> blindSystemTiers = Arrays.stream(SystemTierType.values())
				.filter(stt -> session.getBlindTiers().contains(stt.getName())).toList();

		final Record retVal = createRecord(blindSystemTiers, systemTierParamMap);

		// add user tiers
		for(TierDescription td:session.getUserTiers()) {
			retVal.putTier(createTier(td));
		}

		return retVal;
	}

	/**
	 * Create a new record with provided system tiers as blind tiers
	 *
	 * @param blindTiers
	 * @return
	 */
	public Record createRecord(List<SystemTierType> blindTiers, Map<SystemTierType, Map<String, String>> systemTierParamMap) {
		final RecordSPI recordImpl = sessionFactoryImpl.createRecord(blindTiers, systemTierParamMap);
		return createRecord(recordImpl);
	}
	
	public Record createRecord(RecordSPI recordImpl) {
		return new Record(recordImpl);
	}
	
	/**
	 * Create a new record with the specified speaker.
	 * 
	 * @param speaker
	 * @return the new record
	 */
	public Record createRecord(Participant speaker) {
		final Record retVal = createRecord();
		retVal.setSpeaker(speaker);
		return retVal;
	}

	/**
	 * Create a new record for given session and speaker
	 *
	 * @param session
	 * @param speaker
	 * @return new record
	 */
	public Record createRecord(Session session, Participant speaker) {
		final Record retVal = createRecord(session);
		retVal.setSpeaker(speaker);
		return retVal;
	}
	
	/**
	 * Clone the given record.
	 * 
	 * @param record
	 * 
	 * @return a new record with the same contents and the given
	 *  record
	 *  
	 */
	public Record cloneRecord(Record record) {
		// easiest wasy of ensure we have a clean clone of a record
		// is to use the write it to an in-memory buffer and read
		// it back in
		
		// setup temp session
		final Session tempSession = createSession("temp", "temp");
		if(record.getSpeaker() != null) {
			tempSession.addParticipant(record.getSpeaker());
			
		}
		
		// add extra tiers
		for(String tierName:record.getUserDefinedTierNames()) {
			final Tier<?> extraTier = record.getTier(tierName);
			final TierDescription td = createTierDescription(tierName, extraTier.getDeclaredType(), extraTier.getTierParameters());
			tempSession.addUserTier(td);
		}
		
		// add record
		tempSession.addRecord(record);
		
		final SessionOutputFactory outputFactory = new SessionOutputFactory();
		final SessionWriter writer = outputFactory.createWriter();
		
		final SessionInputFactory inputFactory = new SessionInputFactory();
		final SessionReader reader = inputFactory.createReader("phonbank", "2.0");
		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writer.writeSession(tempSession, out);
			
			final Session inSession = reader.readSession(new ByteArrayInputStream(out.toByteArray()));
			final Record retVal = inSession.getRecord(0);
			retVal.setUuid(UUID.randomUUID());
			return retVal;
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		return null;
	}
	
	/**
	 * Create a new participant object.
	 * 
	 * @return new participant object
	 */
	public Participant createParticipant() {
		final ParticipantSPI participantImpl = sessionFactoryImpl.createParticipant();
		return createParticipant(participantImpl);
	}
	
	public Participant createParticipant(ParticipantSPI participantImpl) {
		return new Participant(participantImpl);
	}
	
	/**
	 * Create the unknown participant object.
	 * 
	 * @return a new participant object with values setup for
	 * an unknown speaker
	 */
	public Participant createUnknownParticipant() {
		return new Participant( new UnidentifiedParticipant() );
	}

	public Participant createAllParticipant() {
		return new Participant( new AllParticipant() );
	}
	
	/**
	 * Clone participant
	 * 
	 * @param part
	 * 
	 * @return cloned participant
	 */
	public Participant cloneParticipant(Participant part) {
		final Participant retVal = createParticipant();
		retVal.setId(part.getId());
		retVal.setRole(part.getRole());
		
		retVal.setName(part.getName());
		retVal.setEducation(part.getEducation());
		retVal.setGroup(part.getGroup());
		retVal.setSex(part.getSex());
		retVal.setSES(part.getSES());
		retVal.setLanguage(part.getLanguage());
		
		if(part.getBirthDate() != null) {
			retVal.setBirthDate(part.getBirthDate());
		}
		
		if(part.getAge(null) != null) {
			retVal.setAge(part.getAge(null));
		}
		
		return retVal;
	}
	
	/**
	 * Create a new transcriber object.
	 * 
	 * @return new transcriber
	 */
	public Transcriber createTranscriber() {
		final TranscriberSPI transcriberImpl = sessionFactoryImpl.createTranscriber();
		return createTranscriber(transcriberImpl);
	}
	
	public Transcriber createTranscriber(TranscriberSPI transcriberImpl) {
		return new Transcriber(transcriberImpl);
	}

	/**
	 * Create a new media segment
	 */
	public MediaSegment createMediaSegment() {
		final MediaSegmentSPI mediaSegmentImpl = sessionFactoryImpl.createMediaSegment();
		return createMediaSegment(mediaSegmentImpl);
	}
	
	public MediaSegment createMediaSegment(MediaSegmentSPI mediaSegmentImpl) {
		return new MediaSegment(mediaSegmentImpl);
	}

	/**
	 * Create a new tier object from given description
	 *
	 * @param tierDescription
	 * @return tier
	 */
	public Tier<?> createTier(TierDescription tierDescription) {
		return createTier(tierDescription.getName(), tierDescription.getDeclaredType(), tierDescription.getTierParameters(), tierDescription.isExcludeFromAlignment(), tierDescription.isBlind());
	}
	
	/**
	 * Create a new tier object with the specified type and alignment rules
	 * 
	 * @param name
	 * @param type
	 * @return the new tier
	 * @param <T>
	 */
	public <T> Tier<T> createTier(String name, Class<T> type) {
		return createTier(name, type, new HashMap<>(), false);
	}

	/**
	 * Create a new tier object with the specified type and alignment rules
	 *
	 * @param name
	 * @param type
	 * @return the new tier
	 * @param <T>
	 */
	public <T> Tier<T> createTier(String name, Class<T> type, Map<String, String> tierParameters, boolean excludeFromAlignment) {
		return createTier(name, type, tierParameters, excludeFromAlignment, false);
	}

	public <T> Tier<T> createTier(String name, Class<T> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind) {
		final TierSPI<T> tierImpl = sessionFactoryImpl.createTier(name, type, tierParameters, excludeFromAlignment, blind, new ArrayList<>(), null);
		return createTier(type, tierImpl);
	}

	public <T> Tier<T> createTier(Class<T> type, TierSPI<T> tierImpl) {
		return new Tier<T>(tierImpl);
	}
	
	/**
	 * Create a new text tier.
	 * 
	 * @param name
	 * @return the new tier
	 */
	public Tier<TierData> createTier(String name) {
		return createTier(name, TierData.class);
	}
	
	/**
	 * Create a new string tier description.
	 * 
	 * @param name
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name) {
		return createTierDescription(name, TierData.class, new HashMap<>(), false);
	}

	/**
	 * Create a new string tier description.
	 *
	 * @param name
	 * @param excludeFromAlignment
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, boolean excludeFromAlignment) {
		return createTierDescription(name, TierData.class, new HashMap<>(), excludeFromAlignment, false);
	}

	public TierDescription createTierDescription(String name, boolean excludeFromAlignment, boolean blind) {
		return createTierDescription(name, TierData.class, new HashMap<>(), excludeFromAlignment, blind, new ArrayList<>(), null);
	}

	/**
	 * Create tier description.
	 *
	 * @param name
	 * @param type
	 * @param tierParameters
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, Class<?> type, Map<String, String> tierParameters) {
		return createTierDescription(name, type, tierParameters, false);
	}

	/**
	 * Create tier description.
	 *
	 * @param name
	 * @param type
	 * @param tierParameters
	 * @param excludeFromAlignment
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, Class<?> type, Map<String, String> tierParameters, boolean excludeFromAlignment) {
		return createTierDescription(name, type, tierParameters, excludeFromAlignment, false);
	}

	public TierDescription createTierDescription(String name, Class<?> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind) {
		return createTierDescription(name, type, tierParameters, excludeFromAlignment, blind, new ArrayList<>(), null);
	}

	/**
	 * Create tier description.
	 * 
	 * @param name
	 * @param type
	 * @param tierParameters
	 * @param excludeFromAlignment
	 * @param blind
	 * @param subtypeDelim
	 * @param subtypeExpr
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, Class<?> type, Map<String, String> tierParameters, boolean excludeFromAlignment, boolean blind, List<String> subtypeDelim, String subtypeExpr) {
		final TierDescriptionSPI tierDescriptionImpl = sessionFactoryImpl.createTierDescription(name, type, tierParameters, excludeFromAlignment, blind, subtypeDelim, subtypeExpr);
		return createTierDescription(tierDescriptionImpl);
	}

	/**
	 * Create tier description for system tier
	 *
	 * @param systemTier
	 * @return tierDesc
	 */
	public TierDescription createTierDescription(SystemTierType systemTier) {
		return createTierDescription(systemTier, false);
	}

	/**
	 * Create tier description from UserTierType
	 *
	 * @param userTier
	 * @return tierDesc
	 */
	public TierDescription createTierDescription(UserTierType userTier) {
		return createTierDescription(userTier.getPhonTierName(), userTier.getType(),
				new LinkedHashMap<>(), !userTier.isAlignable(), false);
	}

	public TierDescription createTierDescription(SystemTierType systemTier, boolean blind) {
		boolean excludeFromAlignment = SystemTierType.Notes == systemTier;
		return createTierDescription(systemTier.getName(), systemTier.getDeclaredType(), new HashMap<>(), excludeFromAlignment, blind, new ArrayList<>(), null);
	}

	public TierDescription createTierDescription(TierDescriptionSPI tierDescriptionImpl) {
		return new TierDescription(tierDescriptionImpl);
	}
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @return
	 */
	public TierViewItem createTierViewItem(String name) {
		return createTierViewItem(name, true);
	}
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @return
	 */
	public TierViewItem createTierViewItem(String name, boolean visible) {
		return createTierViewItem(name, visible, false);
	}
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @return
	 */
	public TierViewItem createTierViewItem(String name, boolean visible, String font) {
		return createTierViewItem(name, visible, font, false);
	}
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param locked
	 * @return
	 */
	public TierViewItem createTierViewItem(String name, boolean visible, boolean locked) {
		return createTierViewItem(name, visible, "default", locked);
	}
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @param locked
	 * @return
	 */
	public TierViewItem createTierViewItem(String name, boolean visible, String font, boolean locked) {
		final TierViewItemSPI tierViewItemImpl = sessionFactoryImpl.createTierViewItem(name, visible, font, locked);
		return createTierViewItem(tierViewItemImpl);
	}
	
	public TierViewItem createTierViewItem(TierViewItemSPI tierViewItemImpl) {
		return new TierViewItem(tierViewItemImpl);
	}
	
	/**
	 * Get the default tier view for a given session.
	 * 
	 * @param session
	 */
	public List<TierViewItem> createDefaultTierView(Session session) {
		final List<TierViewItem> retVal = new ArrayList<TierViewItem>();
		
		retVal.add(createTierViewItem(SystemTierType.Orthography.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.IPATarget.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.IPAActual.getName(), true, "default", false));

		for(TierDescription tierDesc:session.getUserTiers()) {
			retVal.add(createTierViewItem(tierDesc.getName(), true, "default", false));
		}
		
		return retVal;
	}

}

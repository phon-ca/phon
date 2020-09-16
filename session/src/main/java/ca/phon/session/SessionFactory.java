/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;

import ca.phon.extensions.ExtendableObject;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.SessionWriter;
import ca.phon.session.spi.CommentSPI;
import ca.phon.session.spi.MediaSegmentSPI;
import ca.phon.session.spi.ParticipantSPI;
import ca.phon.session.spi.RecordSPI;
import ca.phon.session.spi.SessionFactorySPI;
import ca.phon.session.spi.SessionMetadataSPI;
import ca.phon.session.spi.SessionSPI;
import ca.phon.session.spi.TierDescriptionSPI;
import ca.phon.session.spi.TierSPI;
import ca.phon.session.spi.TierViewItemSPI;
import ca.phon.session.spi.TranscriberSPI;

/**
 * A factory for creating mutable session objects.
 * 
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
			return null;
		}
	}

	private SessionFactory(SessionFactorySPI impl) {
		super();
		this.sessionFactoryImpl = impl;
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
	
	/**
	 * Setup default tier view for session.
	 * 
	 * @param session
	 */
	public void setupDefaultTierView(Session session) {
		final List<TierViewItem> tierView = new ArrayList<TierViewItem>();
		tierView.add(createTierViewItem(SystemTierType.Orthography.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.IPATarget.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.IPAActual.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.Notes.getName(), true));
		tierView.add(createTierViewItem(SystemTierType.Segment.getName(), true));
		
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
		copySessionMetadata(session, retVal);
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
		dest.setLanguage(session.getLanguage());
	}
	
	/**
	 * Create comment
	 * 
	 * 
	 */
	public Comment createComment() {
		final CommentSPI commentImpl = sessionFactoryImpl.createComment();
		return createComment(commentImpl);
	}
	
	public Comment createComment(CommentSPI commentImpl) {
		return new Comment(commentImpl);
	}
	
	/**
	 * Create comment
	 * 
	 * @param tar
	 * @param value
	 * 
	 * @return new comment
	 */
	public Comment createComment(String tag, String value) {
		return createComment(tag, value, null);
	}
	
	public Comment createComment(String tag, String value, MediaSegment segment) {
		final Comment retVal = createComment();
		retVal.setTag(tag);
		retVal.setValue(value);
		if(segment != null) retVal.putExtension(MediaSegment.class, segment);
		return retVal;
	}
	
	public Comment cloneComment(Comment comment) {
		final Comment retVal = createComment();
		retVal.setTag(comment.getTag());
		retVal.setValue(comment.getValue());
		return retVal;
	}
	
	/**
	 * Create session metadata object.
	 * 
	 * @return session metadata
	 */
	public SessionMetadata createSessionMetadata() {
		final SessionMetadataSPI sessionMetadataImpl = sessionFactoryImpl.createSessionMetadata();
		return createSessionMetadata(sessionMetadataImpl);
	}
	
	public SessionMetadata createSessionMetadata(SessionMetadataSPI sessionMetadataImpl) {
		return new SessionMetadata(sessionMetadataImpl);
	}
	
	/**
	 * Clone session metadata
	 * 
	 * @param metadata
	 */
	public void copySessionMetadata(Session session, Session dest) {
		final SessionMetadata metadata = session.getMetadata();
		final SessionMetadata retVal = dest.getMetadata();
		
		retVal.setAppID(metadata.getAppID());
		retVal.setContributor(metadata.getContributor());
		retVal.setCoverage(metadata.getCoverage());
		retVal.setCreator(metadata.getCreator());
		retVal.setDate(metadata.getDate());
		retVal.setDescription(metadata.getDescription());
		retVal.setFormat(metadata.getFormat());
		retVal.setIdentifier(metadata.getIdentifier());
		retVal.setLanguage(metadata.getLanguage());
		retVal.setPublisher(metadata.getPublisher());
		retVal.setRelation(metadata.getRelation());
		retVal.setRights(metadata.getRights());
		retVal.setSource(metadata.getSource());
		retVal.setSubject(metadata.getSubject());
		retVal.setTitle(metadata.getTitle());
		retVal.setType(metadata.getType());
		retVal.setSubject(metadata.getSubject());
		
		for(int i = 0; i < metadata.getNumberOfComments(); i++) {
			final Comment c = metadata.getComment(i);
			final Comment clonedC = cloneComment(c);
			retVal.addComment(clonedC);
		}
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
					createTierDescription(tierDesc.getName(), tierDesc.isGrouped(), tierDesc.getDeclaredType());
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
	
	public Record createRecord(RecordSPI recordImpl) {
		return new Record(recordImpl);
	}
	
	/**
	 * Create group object for given record and index
	 * 
	 * @param r
	 * @param gIdx
	 * @return
	 */
	public Group createGroup(Record r, int gIdx) {
		return new Group(r, gIdx);
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
		for(String tierName:record.getExtraTierNames()) {
			final Tier<String> extraTier = record.getTier(tierName, String.class);
			
			final TierDescription td = createTierDescription(tierName, extraTier.isGrouped());
			tempSession.addUserTier(td);
		}
		
		// add record
		tempSession.addRecord(record);
		
		final SessionOutputFactory outputFactory = new SessionOutputFactory();
		final SessionWriter writer = outputFactory.createWriter();
		
		final SessionInputFactory inputFactory = new SessionInputFactory();
		final SessionReader reader = inputFactory.createReader("phonbank", "1.2");
		
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
	
	/**
	 * Clone participant
	 * 
	 * @param participant
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
	 * Create a new tier object with the specified type.
	 * 
	 * @param name
	 * @param type
	 * @param grouped
	 * @return the new tier
	 */
	public <T> Tier<T> createTier(String name, Class<T> type, boolean grouped) { 
		final TierSPI<T> tierImpl = sessionFactoryImpl.createTier(name, type, grouped);
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
	public Tier<TierString> createTier(String name) {
		return createTier(name, TierString.class, true);
	}
	
	/**
	 * Create a new string tier description.
	 * 
	 * @param name
	 * @param grouped
	 * 
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, boolean grouped) {
		return createTierDescription(name, grouped, TierString.class);
	}
	
	/**
	 * Create tier description.
	 * 
	 * @param name
	 * @param grouped
	 * @param type
	 * 
	 * @return new tier description
	 */
	public TierDescription createTierDescription(String name, boolean grouped, Class<?> type) {
		final TierDescriptionSPI tierDescriptionImpl = sessionFactoryImpl.createTierDescription(name, grouped, type);
		return createTierDescription(tierDescriptionImpl);
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
	 * Get the default tier view for a given sesion.
	 * 
	 * @param session
	 */
	public List<TierViewItem> createDefaultTierView(Session session) {
		final List<TierViewItem> retVal = new ArrayList<TierViewItem>();
		
		retVal.add(createTierViewItem(SystemTierType.Orthography.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.IPATarget.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.IPAActual.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.Notes.getName(), true, "default", false));
		retVal.add(createTierViewItem(SystemTierType.Segment.getName(), true, "default", false));
		
		for(TierDescription tierDesc:session.getUserTiers()) {
			retVal.add(createTierViewItem(tierDesc.getName(), true, "default", false));
		}
		
		return retVal;
	}
}

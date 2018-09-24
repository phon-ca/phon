/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import ca.phon.session.io.SessionInputFactory;
import ca.phon.session.io.SessionOutputFactory;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.SessionWriter;

/**
 * A factory for creating mutable session objects.
 * 
 * 
 */
public abstract class SessionFactory {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(SessionFactory.class.getName());
	
	/**
	 * Create a new session factory.
	 * 
	 * @return factory using first available
	 *  implementation. <code>null</code> if no
	 *  implementation found.
	 */
	public static SessionFactory newFactory() {
		final ServiceLoader<SessionFactory> sessionFactory = 
				ServiceLoader.load(SessionFactory.class);
		final Iterator<SessionFactory> itr = sessionFactory.iterator();
		if(itr.hasNext()) {
			return itr.next();
		} else {
			return null;
		}
	}

	/**
	 * Create a new empty session.
	 * Tier view 
	 * 
	 * @return a new session object
	 */
	public abstract Session createSession();
	
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
	public abstract Comment createComment();
	
	/**
	 * Create comment
	 * 
	 * @param type
	 * @param value
	 * 
	 * @return new comment
	 */
	public Comment createComment(CommentEnum type, String value) {
		return createComment(type, value, null);
	}
	
	public Comment createComment(CommentEnum type, String value, MediaSegment segment) {
		final Comment retVal = createComment();
		retVal.setType(type);
		retVal.setValue(value);
		if(segment != null) retVal.putExtension(MediaSegment.class, segment);
		return retVal;
	}
	
	public Comment cloneComment(Comment comment) {
		final Comment retVal = createComment();
		retVal.setType(comment.getType());
		retVal.setValue(comment.getValue());
		return retVal;
	}
	
	/**
	 * Create session metadata object.
	 * 
	 * @return session metadata
	 */
	public abstract SessionMetadata createSessionMetadata();
	
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
	public abstract Record createRecord();
	
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
	public abstract Participant createParticipant();
	
	/**
	 * Create the unknown participant object.
	 * 
	 * @return a new participant object with values setup for
	 * an unknown speaker
	 */
	public Participant createUnknownParticipant() {
		return new UnidentifiedParticipant();
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
	public abstract Transcriber createTranscriber();
	
	/**
	 * Create a new media segment
	 */
	public abstract MediaSegment createMediaSegment();
	
	/**
	 * Create a new tier object with the specified type.
	 * 
	 * @param name
	 * @param type
	 * @param grouped
	 * @return the new tier
	 */
	public abstract <T> Tier<T> createTier(String name, Class<T> type, boolean grouped);
	
//	/**
//	 * Create a new tier object with the given description.
//	 * 
//	 * @param tierDescription
//	 * @return the new tier
//	 */
//	public <T> Tier<T> createTier(TierDescription tierDescription) {
//		return createTier(tierDescription.getName(), tierDescription.getDeclaredType(), tierDescription.isGrouped());
//	}
	
	/**
	 * Create a new text tier.
	 * 
	 * @param name
	 * @return the new tier
	 */
	public Tier<String> createTier(String name) {
		return createTier(name, String.class, true);
	}
	
	/**
	 * Create a new string tier description.
	 * 
	 * @param name
	 * @param grouped
	 * 
	 * @return new tier description
	 */
	public abstract TierDescription createTierDescription(String name, boolean grouped);
	
	/**
	 * Create tier description.
	 * 
	 * @param name
	 * @param grouped
	 * @param type
	 * 
	 * @return new tier description
	 */
	public abstract TierDescription createTierDescription(String name, boolean grouped, Class<?> type);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @return
	 */
	public abstract TierViewItem createTierViewItem(String name);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @return
	 */
	public abstract TierViewItem createTierViewItem(String name, boolean visible);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @return
	 */
	public abstract TierViewItem createTierViewItem(String name, boolean visible, String font);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param locked
	 * @return
	 */
	public abstract TierViewItem createTierViewItem(String name, boolean visible, boolean locked);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @param locked
	 * @return
	 */
	public abstract TierViewItem createTierViewItem(String name, boolean visible, String font, boolean locked);
	
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

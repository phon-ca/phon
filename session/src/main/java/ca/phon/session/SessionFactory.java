package ca.phon.session;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * A factory for creating mutable session objects.
 * 
 * 
 */
public abstract class SessionFactory {
	
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
	 * 
	 * @return a new session object
	 */
	public abstract Session createSession();
	
	/**
	 * Create a new session with the specified
	 * corpus and name.
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
		return retVal;
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
		final Comment retVal = createComment();
		retVal.setType(type);
		retVal.setValue(value);
		return retVal;
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
	 * Create a new participant object.
	 * 
	 * @return new participant object
	 */
	public abstract Participant createParticipant();
	
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
}

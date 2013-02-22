package ca.phon.session;

/**
 * A factory for session objects.
 * 
 * 
 */
public abstract class SessionFactory {

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
	 * Create a new media segment
	 */
	public abstract MediaSegment createMediaSegment();
	
	/**
	 * Create a new tier object with the specified type.
	 * 
	 * @param type
	 * @return the new tier
	 */
	public abstract <T> Tier<T> createTier(Class<T> type);
	
	/**
	 * Create a new text tier.
	 * 
	 * @return the new tier
	 */
	public Tier<String> createTier() {
		return createTier(String.class);
	}
	
	/**
	 * Create tier order item
	 * 
	 * @return new tier order item
	 */
	public abstract TierOrderItem createTierOrderItem();
	
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
}

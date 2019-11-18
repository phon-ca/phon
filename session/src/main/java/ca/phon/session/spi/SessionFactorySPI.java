package ca.phon.session.spi;

import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.SessionMetadata;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;

public interface SessionFactorySPI {

	/**
	 * Create a new empty session.
	 * Tier view 
	 * 
	 * @return a new session impl
	 */
	public SessionSPI createSession();
	
	/**
	 * Create comment
	 * 
	 * @return new comment impl
	 */
	public CommentSPI createComment();
	
	/**
	 * Session metadata
	 * 
	 * @return
	 */
	public SessionMetadataSPI createSessionMetadata();

	/**
	 * Create a new record.
	 * 
	 * @return a new empty record
	 */
	public RecordSPI createRecord();
	
	/**
	 * Create a new participant object.
	 * 
	 * @return new participant object
	 */
	public ParticipantSPI createParticipant();
	
	/**
	 * Create a new transcriber object.
	 * 
	 * @return new transcriber
	 */
	public TranscriberSPI createTranscriber();
	
	/**
	 * Create a new media segment
	 */
	public MediaSegmentSPI createMediaSegment();
	
	/**
	 * Create a new tier object with the specified type.
	 * 
	 * @param name
	 * @param type
	 * @param grouped
	 * @return the new tier
	 */
	public <T> TierSPI<T> createTier(String name, Class<T> type, boolean grouped);
	
	/**
	 * Create tier description.
	 * 
	 * @param name
	 * @param grouped
	 * @param type
	 * 
	 * @return new tier description
	 */
	public TierDescriptionSPI createTierDescription(String name, boolean grouped, Class<?> type);
	
	/**
	 * Create a tier display and ordering object
	 * @param name
	 * @param visible
	 * @param font
	 * @param locked
	 * @return
	 */
	public TierViewItemSPI createTierViewItem(String name, boolean visible, String font, boolean locked);
	
}

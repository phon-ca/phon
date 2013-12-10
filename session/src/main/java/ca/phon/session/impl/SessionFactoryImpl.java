package ca.phon.session.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.session.Comment;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.session.Transcriber;

/**
 * Default implementation of a session factory.
 */
public class SessionFactoryImpl extends SessionFactory {
	
	private static final Logger LOGGER = Logger
			.getLogger(SessionFactoryImpl.class.getName());

	@Override
	public Session createSession() {
		return new SessionImpl();
	}

	@Override
	public Record createRecord() {
		return new RecordImpl();
	}

	@Override
	public Participant createParticipant() {
		return new ParticipantImpl();
	}

	@Override
	public MediaSegment createMediaSegment() {
		return new MediaSegmentImpl();
	}

	@Override
	public <T> Tier<T> createTier(String name, Class<T> type, boolean grouped) {
		final TierImpl<T> retVal = new TierImpl<>(name, type, grouped);
		return retVal;
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped) {
		return new TierDescriptionImpl(name, grouped);
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped,
			Class<?> type) {
		return new TierDescriptionImpl(name, grouped, type);
	}

	@Override
	public Transcriber createTranscriber() {
		return new TranscriberImpl();
	}

	@Override
	public TierViewItem createTierViewItem(String name) {
		return new TierViewItemImpl(name);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible) {
		return new TierViewItemImpl(name, visible);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			String font) {
		return new TierViewItemImpl(name, visible, font);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			boolean locked) {
		return new TierViewItemImpl(name, visible, locked);
	}

	@Override
	public TierViewItem createTierViewItem(String name, boolean visible,
			String font, boolean locked) {
		return new TierViewItemImpl(name, visible, font, locked);
	}

	@Override
	public Comment createComment() {
		return new CommentImpl();
	}

}

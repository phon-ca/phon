package ca.phon.session.impl;

import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierOrderItem;

/**
 * Default implementation of a session factory.
 */
public class SessionFactoryImpl extends SessionFactory {

	@Override
	public Session createSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Record createRecord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Participant createParticipant() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MediaSegment createMediaSegment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Tier<T> createTier(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TierOrderItem createTierOrderItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TierDescription createTierDescription(String name, boolean grouped,
			Class<?> type) {
		// TODO Auto-generated method stub
		return null;
	}

}

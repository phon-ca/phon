package ca.phon.session.impl;

import java.util.Iterator;

import ca.phon.session.Participant;
import ca.phon.session.Participants;

/**
 * Implementation of the {@link Participants} helper class.
 */
public class ParticipantsImpl extends Participants {

	private final SessionImpl sessionImpl;
	
	ParticipantsImpl(SessionImpl sessionImpl) {
		super();
		this.sessionImpl = sessionImpl;
	}
	
	@Override
	public Iterator<Participant> iterator() {
		return new ParticipantsIterator();
	}
	
	/**
	 * Iterator session participants
	 */
	private final class ParticipantsIterator implements Iterator<Participant> {
		
		private int currentParticipant = 0;

		@Override
		public boolean hasNext() {
			return currentParticipant < sessionImpl.getParticipantCount();
		}

		@Override
		public Participant next() {
			return sessionImpl.getParticipant(currentParticipant++);
		}

		@Override
		public void remove() {
			sessionImpl.removeParticipant(currentParticipant-1);
		}
		
	}

}

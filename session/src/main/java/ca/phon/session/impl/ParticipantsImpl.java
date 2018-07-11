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

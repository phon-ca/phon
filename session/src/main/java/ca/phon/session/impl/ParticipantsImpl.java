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

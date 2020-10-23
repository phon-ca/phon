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

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.visitor.*;

/**
 * Helper class providing iterator and visitor methods
 * for {@link Session} {@link Participant}s.
 */
public final class Participants extends ExtendableObject implements Iterable<Participant>, Visitable<Participant> {

	private final Session session;
	
	Participants(Session session) {
		super();
		this.session = session;
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
			return currentParticipant < session.getParticipantCount();
		}

		@Override
		public Participant next() {
			return session.getParticipant(currentParticipant++);
		}

		@Override
		public void remove() {
			session.removeParticipant(currentParticipant-1);
		}
		
	}
	
	@Override
	public void accept(Visitor<Participant> visitor) {
		for(Participant p:this) {
			visitor.visit(p);
		}
	}
	
	public Map<ParticipantRole, Integer> getRoleCount() {
		final Map<ParticipantRole, Integer> retVal = new HashMap<ParticipantRole, Integer>();
		
		for(Participant p:this) {
			Integer rc = retVal.get(p.getRole());
			if(rc == null) {
				rc = 0;
			}
			rc++;
			retVal.put(p.getRole(), rc);
		}
		
		return retVal;
	}
	
	/**
	 * Returns a list of participants which does not include
	 * the given participant.
	 * 
	 * @param part
	 * @return
	 */
	public List<Participant> otherParticipants(Participant part) {
		List<Participant> retVal = new ArrayList<Participant>();
		for(Participant p:this) {
			if(p == part) continue;
			retVal.add(p);
		}
		return retVal;
	}
	
	public static void copyParticipantInfo(Participant src, Participant dest) {
		dest.setId(src.getId());
		dest.setBirthDate(src.getBirthDate());
		dest.setAge(src.getAge(null));
		dest.setEducation(src.getEducation());
		dest.setGroup(src.getGroup());
		dest.setLanguage(src.getLanguage());
		dest.setName(src.getName());
		dest.setRole(src.getRole());
		dest.setSES(src.getSES());
		dest.setSex(src.getSex());
	}
}

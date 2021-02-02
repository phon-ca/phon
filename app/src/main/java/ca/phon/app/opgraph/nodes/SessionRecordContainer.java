/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes;

import java.util.*;

import ca.phon.session.*;
import ca.phon.session.Record;

public class SessionRecordContainer implements RecordContainer {
	
	private Session session;

	private Collection<Participant> selectedParticipants = List.of(Participant.ALL);

	public SessionRecordContainer(Session session) {
		super();
		this.session = session;
	}

	public SessionRecordContainer(Session session, Collection<Participant> selectedParticipants) {
		super();
		this.session = session;
		this.selectedParticipants = selectedParticipants;
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Iterator<Integer> idxIterator() {
		return new SelectedParticipantsRecordContainer();
	}

	private class SelectedParticipantsRecordContainer implements Iterator<Integer> {

		private int currentRecord = 0;

		@Override
		public boolean hasNext() {
			if(selectedParticipants.size() == 1 && selectedParticipants.iterator().next() == Participant.ALL) {
				return currentRecord < session.getRecordCount();
			} else {
				while (currentRecord < session.getRecordCount()) {
					Record r = session.getRecord(currentRecord);
					Participant speaker = r.getSpeaker();
					Optional<Participant> selectedVersion = selectedParticipants.stream()
							.filter(p -> {
								if (p.getId().equals(speaker.getId())) {
									if (p.getName() == null && speaker.getName() == null) {
										return true;
									} else {
										return (p.getName() != null && p.getName().equals(speaker.getName()));
									}
								} else {
									return false;
								}
							}).findFirst();
					if (selectedVersion.isPresent()) {
						return true;
					} else {
						++currentRecord;
					}
				}
				return false;
			}
		}

		@Override
		public Integer next() {
			return currentRecord++;
		}

	}

}

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

import java.util.ArrayList;
import java.util.List;

/**
 * Filter utterances by speaker.  If 'null' is given during construction
 * than searches for utterances with no speaker.
 */
public class ParticipantRecordFilter extends AbstractRecordFilter {
	
	/**
	 * participant 
	 */
	private List<Participant> participants
		= new ArrayList<Participant>();
	
	public ParticipantRecordFilter() {
		
	}
	
	public ParticipantRecordFilter(Participant p) {
		addParticipant(p);
	}
	
	public ParticipantRecordFilter(List<Participant> parts) {
		participants.addAll(parts);
	}
	
	public void addParticipant(Participant p) {
		if(!participants.contains(p) && p != null) {
			participants.add(p);
		}
	}
	
	public List<Participant> getParticipants() {
		return this.participants;
	}

	@Override
	public boolean checkRecord(Record utt) {
		boolean retVal = false;
		
		if(utt.getSpeaker() == null && this.participants.size() == 0) {
			retVal = true;
		} else if(utt.getSpeaker() != null) {
			for(Participant p:participants) {
				if(p.getId().equals(utt.getSpeaker().getId())) {
					retVal = true;
					break;
				}
			}
		}
		
		return retVal;
	}

}

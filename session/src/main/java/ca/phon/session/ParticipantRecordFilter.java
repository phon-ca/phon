/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

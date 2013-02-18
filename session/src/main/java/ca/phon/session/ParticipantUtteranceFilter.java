/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
public class ParticipantUtteranceFilter extends AbstractUtteranceFilter {
	
	/**
	 * participant 
	 */
	private List<IParticipant> participants
		= new ArrayList<IParticipant>();
	
	public ParticipantUtteranceFilter() {
		
	}
	
	public ParticipantUtteranceFilter(IParticipant p) {
		addParticipant(p);
	}
	
	public ParticipantUtteranceFilter(List<IParticipant> parts) {
		participants.addAll(parts);
	}
	
	public void addParticipant(IParticipant p) {
		if(!participants.contains(p) && p != null) {
			participants.add(p);
		}
	}
	
	public List<IParticipant> getParticipants() {
		return this.participants;
	}

	@Override
	public boolean checkUtterance(IUtterance utt) {
		boolean retVal = false;
		
		if(utt.getSpeaker() == null && this.participants.size() == 0) {
			retVal = true;
		} else if(utt.getSpeaker() != null) {
			for(IParticipant p:participants) {
				if(p.getId().equals(utt.getSpeaker().getId())) {
					retVal = true;
					break;
				}
			}
		}
		
		return retVal;
	}

}

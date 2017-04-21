/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.project;

import java.util.HashMap;
import java.util.Map;

import ca.phon.session.Participant;
import ca.phon.session.ParticipantRole;
import ca.phon.session.Participants;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierDescription;

/**
 * Handle merging of sessions
 *
 */
public class SessionMerger {
	
	private Map<Participant, Participant> participantMap = 
			new HashMap<>();
	
	/**
	 * Merge the given sessions using the given
	 * utterance filters.
	 * 
	 * @param dest
	 * @param src
	 * @param filter
	 */
	public void mergeSession(Session dest, Session src,
			RecordFilter filter) {
		participantMap.clear();
		// add participants first
		addParticipants(dest, src);
		
		mergeDependentTiers(dest, src);
		
		// add selected records
		addRecordsFromSession(dest, src, filter);
	}
	
	/**
	 * Merge dependent tiers.
	 * 
	 */
	public void mergeDependentTiers(Session dest, Session src) {
		final SessionFactory factory = SessionFactory.newFactory();
		for(int i = 0; i < src.getUserTierCount(); i++) {
			final TierDescription tierDesc = src.getUserTier(i);
			// try to find first
			TierDescription newDesc = null;
			for(int j = 0; j < dest.getUserTierCount(); j++) {
				final TierDescription td = dest.getUserTier(j);
				if(td.getName().equals(tierDesc.getName())) {
					newDesc = td;
					break;
				}
			}
			
			if(newDesc == null) {
				// add new dep tier
				newDesc = factory.createTierDescription(tierDesc.getName(), tierDesc.isGrouped());
				dest.addUserTier(newDesc);
			}
		}
	}
	
	/**
	 * Add filtered utterances to new transcript
	 * 
	 * @param dest
	 * @param src
	 * @param filter
	 */
	private void addRecordsFromSession(Session dest, 
			Session src, RecordFilter filter) {
		for(int i = 0; i < src.getRecordCount(); i++) {
			final Record r = src.getRecord(i);
			if(filter.checkRecord(r)) {
				r.setSpeaker(participantMap.get(r.getSpeaker()));
				dest.addRecord(r);
			}
		}
	}
	
	private void addParticipants(Session dest, Session src) {
		final SessionFactory factory = SessionFactory.newFactory();
		// add each participant to the 
		// new transcript.  Make sure not
		// to duplicate names
		for(int i = 0; i < src.getParticipantCount(); i++) {
			final Participant srcPart = src.getParticipant(i);
			final String speakerName = srcPart.toString();
			
			Participant destPart = null;
			for(int j = 0; j < dest.getParticipantCount(); j++) {
				final Participant dp = dest.getParticipant(j);
				if(dp.toString().equalsIgnoreCase(speakerName)) {
					destPart = dp;
					participantMap.put(srcPart, destPart);
					break;
				}
			}
			
			if(destPart == null) {
				String role = srcPart.getRole().getId();
				// ensure unique ids for participants
				Map<ParticipantRole, Integer> roleCount = dest.getParticipants().getRoleCount();
				if(roleCount.get(srcPart.getRole()) != null
						&& roleCount.get(srcPart.getRole()) > 0) {
					int cnt = roleCount.get(srcPart.getRole());
					role = srcPart.getRole().getId().substring(0, 2) + cnt;
				}
				
				destPart = factory.createParticipant();
				Participants.copyParticipantInfo(srcPart, destPart);
				destPart.setId(role);
				
				participantMap.put(srcPart, destPart);
				dest.addParticipant(destPart);
			}
		}
		
	}

}

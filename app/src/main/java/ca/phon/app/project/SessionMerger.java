/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.session.Participant;
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
	
	/**
	 * Merge the given sessions using the given
	 * utterance filters.
	 * 
	 * @param dest
	 * @param src
	 * @param filter
	 */
	public static void mergeSession(Session dest, Session src,
			RecordFilter filter) {
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
	public static void mergeDependentTiers(Session dest, Session src) {
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
	private static void addRecordsFromSession(Session dest, 
			Session src, RecordFilter filter) {
		for(int i = 0; i < src.getRecordCount(); i++) {
			final Record r = src.getRecord(i);
			if(filter.checkRecord(r)) {
				dest.addRecord(r);
			}
		}
	}
	
	private static int partIdx = 0;
	private static void addParticipants(Session dest, Session src) {
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
					break;
				}
			}
			
			if(destPart == null) {
				dest.addParticipant(srcPart);
			}
		}
		
	}

}

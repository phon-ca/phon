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
package ca.phon.app.project;

import java.util.List;

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
	public static void mergeSession(ITranscript dest, ITranscript src,
			UtteranceFilter filter) {
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
	public static void mergeDependentTiers(ITranscript dest, ITranscript src) {
		
		for(IDepTierDesc tierDesc:src.getDependentTiers()) {
			
			// try to find first
			IDepTierDesc newDesc = null;
			for(IDepTierDesc td:dest.getDependentTiers()) {
				if(td.getTierName().equals(tierDesc.getTierName())) {
					newDesc = td;
					break;
				}
			}
			
			if(newDesc == null) {
				// add new dep tier
				newDesc = dest.newDependentTier();
				newDesc.setTierName(tierDesc.getTierName());
				newDesc.setIsGrouped(false);
			}
			
		}
		
		for(IDepTierDesc tierDesc:src.getWordAlignedTiers()) {
			
			// try to find first
			IDepTierDesc newDesc = null;
			for(IDepTierDesc td:dest.getWordAlignedTiers()) {
				if(td.getTierName().equals(tierDesc.getTierName())) {
					newDesc = td;
					break;
				}
			}
			
			if(newDesc == null) {
				// add new dep tier
				newDesc = dest.newDependentTier();
				newDesc.setTierName(tierDesc.getTierName());
				newDesc.setIsGrouped(true);
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
	private static void addRecordsFromSession(ITranscript dest, 
			ITranscript src, UtteranceFilter filter) {
		
		List<IUtterance> utts = src.getUtterances();
		utts = ((AbstractUtteranceFilter)filter).filterUtterances(utts);
		
		
		for(IUtterance utt:utts) {
			IUtterance newUtt = TranscriptUtils.addRecordToTranscript(dest, utt, null);
			
			// make sure the correct participant is assigned
			if(utt.getSpeaker() != null) {
				String speakerName = utt.getSpeaker().getName();
				if(speakerName != null && speakerName.length() > 0) {
					IParticipant p = null;
					for(IParticipant part:dest.getParticipants()) {
						if(part.getName().equalsIgnoreCase(speakerName)) {
							p = part;
							break;
						}
					}
					
					if(p != null) {
						newUtt.setSpeaker(p);
					}
				}
			}
		}
		
	}
	
	private static int partIdx = 0;
	private static void addParticipants(ITranscript dest, ITranscript src) {
		
		// add each participant to the 
		// new transcript.  Make sure not
		// to duplicate names
		for(IParticipant srcPart:src.getParticipants()) {
			
			String speakerName = srcPart.getName();
			
			IParticipant destPart = null;
			for(IParticipant dp:dest.getParticipants()) {
				if(dp.getName().equalsIgnoreCase(speakerName)) {
					destPart = dp;
					break;
				}
			}
			
			if(destPart == null) {
				destPart = dest.newParticipant();
				TranscriptUtils.copyParticipant(srcPart, destPart);
				destPart.setId("p" + (++partIdx));
			}
			
		}
		
	}

}

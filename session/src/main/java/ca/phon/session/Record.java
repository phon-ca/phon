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

import java.util.List;

import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;


/**
 * An utterance.
 * 
 *
 */
public interface Record extends IExtendable, SessionElement {
	
	/**
	 * The participant (speaker)
	 * 
	 * @return IParticipant
	 */
	public Participant getSpeaker();
	
	/**
	 * Set the participant
	 * 
	 * @paran IParticipant
	 */
	public void setSpeaker(Participant participant);
	
	/**
	 * Get media segment
	 * 
	 * @return IMedia
	 */
	public MediaSegment getSegment();
	
	/**
	 * Set media segment
	 * 
	 * @param media
	 */
	public void setSegment(MediaSegment media);
	
	/**
	 * Should we exclude this record from searches?
	 */
	public boolean isExcludeFromSearches();
	
	/**
	 * Set exclusion from searches.
	 * 
	 * @param excluded
	 */
	public void setExcludeFromSearches(boolean excluded);
	
	/**
	 * Get the orthography tier.
	 * 
	 * @return orthography tier
	 */
	public Tier<Orthography> getOrthography();
	
	/**
	 * Set the value of the orthography tier.
	 * 
	 * @param orthography
	 */
	public void setOrthography(Tier<Orthography> ortho);
	
	/**
	 * Get the number of aligned groups in the record.
	 * The number of groups for a record is determined by the number
	 * of groups in the orthoraphy tier.
	 * 
	 * @return number of groups
	 */
	public int numberOfGroups();
	
	/**
	 * Get the group at the specified index
	 * 
	 * @param idx
	 * 
	 * @return the specified group
	 */
	public Group getGroup(int idx);
	
	/**
	 * Delete the group at the specified index.  This will
	 * affect all grouped tiers.
	 * 
	 * @param idx the group to remove
	 */
	public void removeGroup(int idx);
	
	/**
	 * Merge groups from the given start index
	 * to the given end index. This will affect
	 * all grouped tiers.
	 * 
	 * @param startIdx
	 * @param endIdx
	 */
	public void mergeGroups(int startIdx, int endIdx);
	
	/**
	 * Split the specified group at the given orthography word index.
	 * To split the group at the beginning use index 0,
	 * to split the group at the end use (number of elements in orthography)+1.
	 * 
	 * @param groupIdx
	 * @param wordIdx
	 */
	public void splitGroup(int groupIdx, int wordIdx);
	
	/**
	 * Get the IPA Target tier
	 * 
	 * @return the ipa target tier
	 */
	public Tier<IPATranscript> getIPATarget();
	
	/**
	 * Set the value of the IPA Actual tier
	 * 
	 * @param ipa
	 */
	public void setIPATarget(Tier<IPATranscript> ipa);
	
	/**
	 * Get the IPA Actual tier
	 * 
	 * @return the ipa actual tier
	 */
	public Tier<IPATranscript> getIPAActual();
	
	/**
	 * Set the IPA Actual tier
	 * 
	 * @param ipa
	 */
	public void setIPAActual(Tier<IPATranscript> ipa);
	
	/**
	 * Get the value of the notes tier
	 * 
	 * @return the notes tier
	 */
	public Tier<String> getNotes();
	
	/**
	 * Set the value of the notes tier
	 * 
	 * @param notes
	 */
	public void setNotes(Tier<String> notes);
	
	/**
	 * Get the given tier with the expected type.
	 * 
	 * @param name
	 * @param type
	 * 
	 * @return the specified tier or <code>null</code> if a tier
	 *  with the given name and type are not found.
	 */
	public <T> Tier<T> getTier(String name, Class<T> type);
	
	/**
	 * Get the given tier as a text tier.
	 * Every tier can be represented in this way.
	 * 
	 * @param name
	 * @return the tier data represented as a string
	 */
	public Tier<String> getTier(String name);
	
	/**
	 * Remove the dependent tier with the given name.
	 * 
	 * @param name
	 */
	public void removeTier(String name);
	
}

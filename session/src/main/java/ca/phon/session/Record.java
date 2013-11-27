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

import java.util.Set;
import java.util.UUID;

import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;


/**
 * 
 */
public interface Record extends IExtendable {
	
	/**
	 * Record id
	 * 
	 * @param unique id for this record
	 */
	public UUID getUuid();
	
	/**
	 * Set id for the record
	 */
	public void setUuid(UUID id);
	
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
	public Tier<MediaSegment> getSegment();
	
	/**
	 * Set media segment
	 * 
	 * @param media
	 */
	public void setSegment(Tier<MediaSegment> media);
	
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
	
//	/**
//	 * Merge groups from the given start index
//	 * to the given end index. This will affect
//	 * all grouped tiers.
//	 * 
//	 * @param startIdx
//	 * @param endIdx
//	 */
//	public void mergeGroups(int startIdx, int endIdx);
//	
//	/**
//	 * Split the specified group at the given orthography word index.
//	 * To split the group at the beginning use index 0,
//	 * to split the group at the end use (number of elements in orthography)+1.
//	 * 
//	 * @param groupIdx
//	 * @param wordIdx
//	 */
//	public void splitGroup(int groupIdx, int wordIdx);
	
	/**
	 * Add a new group to the end of the record data
	 * 
	 * @return the new group
	 */
	public Group addGroup();
	
	/**
	 * Add a new group at the specified index.  This method
	 * will add a new group value to each grouped tier.
	 * 
	 * @param idx
	 * @return the new group
	 * 
	 * @throws ArrayIndexOutOfBoundsException if idx is out of bounds
	 */
	public Group addGroup(int idx);
	
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
	 * Get the phone alignment between IPA Target and IPA Actual
	 * 
	 * @return phone alignment
	 */
	public Tier<PhoneMap> getPhoneAlignment();
	
	/**
	 * Set the phone alignment
	 * 
	 * @param phoneAlignment
	 */
	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment);
	
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
	 * Get the register type of the given tier.
	 * 
	 * @param tier name
	 * @return the tier type
	 */
	public Class<?> getTierType(String name);
	
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
	 * Get the given tier with unspecified typing.
	 * 
	 * @return name
	 */
	public Tier<?> getTier(String name);
	
	/**
	 * Return a list of user-defined tiers that are present
	 * in this record.
	 * 
	 * @return the list of tier user-defined tier names
	 *  present in this record
	 */
	public Set<String> getExtraTierNames();
	
	/**
	 * @param tier name
	 * @return <code>true</code> if this record contains
	 *  the specified tier
	 */
	public boolean hasTier(String name);
	
	/**
	 * Add/set the given tier to the list of user defined
	 * tiers.
	 * 
	 * @param tier
	 */
	public void putTier(Tier<?> tier);
	
	/**
	 * Remove the dependent tier with the given name.
	 * 
	 * @param name
	 */
	public void removeTier(String name);
	
	/**
	 * number of comments
	 * 
	 * @return number of comments
	 */
	public int getNumberOfComments();
	
	/**
	 * get comment at given index
	 * 
	 * @param idx
	 * @return comment
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Comment getComment(int idx);
	
	/**
	 * Add comment
	 * 
	 * @param comment
	 */
	public void addComment(Comment comment);
	
	/**
	 * Remove comment
	 * 
	 * @param comment
	 */
	public void removeComment(Comment comment);
	public void removeComment(int idx);
	
}

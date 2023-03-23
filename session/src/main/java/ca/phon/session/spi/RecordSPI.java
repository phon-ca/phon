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
package ca.phon.session.spi;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;

import java.util.*;

public interface RecordSPI {
	
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
	 * The participant (speaker).
	 * 
	 * As of Phon 2.2, this method will return
	 * {@link Participant#UNKNOWN} instead of <code>null</code>
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
	 * Return media segment for record
	 *
	 * @return media segment
	 */
	public MediaSegment getMediaSegment();

	/**
	 * Set media segment for record
	 * @param segment
	 */
	public void setMediaSegment(MediaSegment segment);

	/**
	 * Get media segment tier
	 *
	 * @return segment tier for record, always available
	 */
	public Tier<MediaSegment> getSegment();

	/**
	 * Get group segment tier
	 *
	 * @return group segment tier for record, may be null
	 */
	public Tier<GroupSegment> getGroupSegment();
	
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
	public Tier<TierString> getNotes();
	
	/**
	 * Set the value of the notes tier
	 * 
	 * @param notes
	 */
	public void setNotes(Tier<TierString> notes);
	
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
	 * Return a list of all present tiers which have the given
	 * type.
	 * 
	 * @param type
	 * @return list of tiers 
	 */
	public <T> List<Tier<T>> getTiersOfType(Class<T> type);
	
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

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

import ca.phon.session.*;
import ca.phon.util.Language;

import java.time.LocalDate;
import java.util.List;

/**
 * service provider interface for sessions
 *
 */
public interface SessionSPI {

	/** Get the corpus */
	public String getCorpus();
	
	/** Get the transcript name */
	public String getName();

	/** Get the transcript date */
	public LocalDate getDate();
	
	/**
	 * Get the list of (unmodifiable) languages (if any) for the session
	 */
	public List<Language> getLanguages();

	/**
	 * Set the list of languages for the session
	 *
	 * @param languages
	 */
	public void setLanguages(List<Language> languages);
	
	/** Get/Set the media file location */
	public String getMediaLocation();

	/**
	 * Get the tier view
	 *
	 * @return tier view (unmodifiable)
	 */
	public List<TierViewItem> getTierView();

	/**
	 * Set tier view
	 *
	 * @param view
	 */
	public void setTierView(List<TierViewItem> view);
	
	/*
	 * Custom tiers defined for the session
	 */
	/**
	 * Number of user-defined tiers for this session
	 */
	public int getUserTierCount();
	
	/**
	 * Get user tier for the specified index.
	 * 
	 * @param idx
	 * 
	 * @return tier description 
	 */
	public TierDescription getUserTier(int idx);
	
	/**
	 * Remove user tier 
	 * 
	 * @param idx
	 */
	public TierDescription removeUserTier(int idx);
	
	public TierDescription removeUserTier(TierDescription tierDescription);
	
	/**
	 * Add a user tier
	 */
	public void addUserTier(TierDescription tierDescription);

	public void addUserTier(int idx, TierDescription tierDescription);
	
	/**
	 * Get the number of transcribers
	 */
	public int getTranscriberCount();
	
	/**
	 * Get transcriber for the specified username
	 * @param username
	 * @return
	 */
	public Transcriber getTranscriber(String username);
	
	/**
	 * Get the <code>i</code>th transcriber.
	 * @param i
	 * @return
	 */
	public Transcriber getTranscriber(int i);
	
	/**
	 * Remove the <code>i</code>th transcriber
	 * 
	 * @param i
	 */
	public void removeTranscriber(int i);
	
	/**
	 * Get the metadata
	 * 
	 * @return Metadata
	 */
	public SessionMetadata getMetadata();
	
	/**
	 * Get the number of participants
	 * 
	 * @return the number of participants
	 */
	public int getParticipantCount();

	/**
	 * Add a new participant
	 * 
	 * @param participant
	 */
	public void addParticipant(Participant participant);

	/**
	 * Add participant at given index
	 *
	 * @param idx
	 * @param participant
	 *
	 */
	public void addParticipant(int idx, Participant participant);
	
	/**
	 * Get the participant at the given index
	 * 
	 * @param idx
	 * @return the specified participant
	 */
	public Participant getParticipant(int idx);

	/**
	 * Get the index of the given participant
	 *
	 * @return index of participant or -1 if not found
	 */
	public int getParticipantIndex(Participant participant);

	/** Set the corpus */
	public void setCorpus(String corpus);
	
	/** Set the transcript name */
	public void setName(String name);
	
	/** Get the transcript date */
	public void setDate(LocalDate date);
	
	/** Media location */
	public void setMediaLocation(String mediaLocation);
	
	/**
	 * Add a new transcriber
	 */
	public void addTranscriber(Transcriber t);
	
	/**
	 * Remove a transcriber
	 */
	public void removeTranscriber(Transcriber t);
	public void removeTranscriber(String username);
	
	/**
	 * Remove a participant.
	 * 
	 * @param participant
	 */
	public void removeParticipant(Participant participant);
	
	/**
	 * Remove a participant
	 * 
	 * @param idx
	 */
	public void removeParticipant(int idx);

	public Transcript getTranscript();
	
}

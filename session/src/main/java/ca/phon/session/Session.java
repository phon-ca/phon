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
package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.session.spi.SessionSPI;
import ca.phon.util.Language;

import java.time.LocalDate;
import java.util.List;

/**
 * A session, usually recording session, with participants, optional media and
 * transcript. The transcript is a collection of interleaved comments and records.
 * Each record is composed of a number of tiers, with the 'Orthography' tier transcribing
 * a spoken utterance or event.
 *
 */
public final class Session extends ExtendableObject {

	private SessionSPI sessionImpl;
	
	Session(SessionSPI sessionImpl) {
		super();
		this.sessionImpl = sessionImpl;
	}
	
	/* Delegates */
	/**
	 * @return corpus name
	 */
	public String getCorpus() {
		return sessionImpl.getCorpus();
	}

	public void setCorpus(String corpus) {
		sessionImpl.setCorpus(corpus);
	}

	/**
	 * @return session name
	 */
	public String getName() {
		return sessionImpl.getName();
	}

	public void setName(String name) {
		sessionImpl.setName(name);
	}

	/**
	 * @return session recording date, may be null
	 */
	public LocalDate getDate() {
		return sessionImpl.getDate();
	}

	public void setDate(LocalDate date) {
		sessionImpl.setDate(date);
	}

	/**
	 * Return list of languages contained in the session
	 *
	 * @return list of session languages
	 */
	public List<Language> getLanguages() {
		return sessionImpl.getLanguages();
	}

	public void setLanguages(List<Language> languages) {
		sessionImpl.setLanguages(languages);
	}

	/**
	 * Get media location. Media location may be an absolute path or a relative path.
	 * Relative paths are searched using folder options as setup in the application.
	 *
	 * @return media location, may be null
	 */
	public String getMediaLocation() {
		return sessionImpl.getMediaLocation();
	}

	public void setMediaLocation(String mediaLocation) {
		sessionImpl.setMediaLocation(mediaLocation);
	}

	/* Tier View */
	/**
	 * Get tier view for session. Tier view controls tier ordering, visiblity,
	 * font, and locked status.  Tier view includes both default tiers and
	 * user-defined tiers.
	 *
	 * @return tier view
	 */
	public List<TierViewItem> getTierView() {
		return sessionImpl.getTierView();
	}

	/**
	 * Set tier view for session.  Tiew view must include all default tiers along
	 * with any user-defined tiers.
	 *
	 * @param view
	 * @throws IllegalArgumentException if tier view does not include all necessary tiers
	 */
	public void setTierView(List<TierViewItem> view) {
		sessionImpl.setTierView(view);
	}

	/* User-defined tiers */
	/**
	 * Get number of user-defined tiers setup in session
	 *
	 * @return user tier count
	 */
	public int getUserTierCount() {
		return sessionImpl.getUserTierCount();
	}

	/**
	 * Get tier description for user-defined tier at given index
	 *
	 * @param idx
	 * @return user-defined tier description
	 * @throws ArrayIndexOutOfBoundsException
 	 */
	public TierDescription getUserTier(int idx) {
		return sessionImpl.getUserTier(idx);
	}

	/**
	 * Remove user-defined tier at given idx from the session tier list.  Note, this does
	 * not remove any tier data from individual records.
	 *
	 * @param idx
	 * @return tier description that was removed
	 * @throws IllegalArgumentException
	 */
	public TierDescription removeUserTier(int idx) {
		return sessionImpl.removeUserTier(idx);
	}

	/**
	 * Remove the user-defined tier description from the session tier list. Note, this does
	 * not remove any tier data from individual records.
	 *
	 * @param tierDescription
	 * @return tier description that was removed
	 */
	public TierDescription removeUserTier(TierDescription tierDescription) {
		return sessionImpl.removeUserTier(tierDescription);
	}

	/**
	 * Add user-defined tier description to end of the session tier list. Note, this does
	 * not add the tier to individual records.
	 *
	 * @param tierDescription
	 * @throws IllegalArgumentException on tier name conflict
	 */
	public void addUserTier(TierDescription tierDescription) {
		sessionImpl.addUserTier(tierDescription);
	}

	/**
	 * Add user-defined tier description at specified index in tier list. Note, this does
	 * not add the tier to individual records.
	 *
	 * @param idx
	 * @param tierDescription
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException on tier name conflict
	 */
	public void addUserTier(int idx, TierDescription tierDescription) {
		sessionImpl.addUserTier(idx, tierDescription);
	}

	/**
	 * Return list of user-defined tier descriptions.
	 *
	 * @return tier descriptions for user-defined tiers in a wrapper class
	 */
	public TierDescriptions getUserTiers() {
		return new TierDescriptions(this);
	}

	/* Transcribers (blind transcription) */
	public int getTranscriberCount() {
		return sessionImpl.getTranscriberCount();
	}

	public void addTranscriber(Transcriber t) {
		sessionImpl.addTranscriber(t);
	}

	public void removeTranscriber(Transcriber t) {
		sessionImpl.removeTranscriber(t);
	}

	public void removeTranscriber(String username) {
		sessionImpl.removeTranscriber(username);
	}

	public Transcriber getTranscriber(String username) {
		return sessionImpl.getTranscriber(username);
	}

	public Transcriber getTranscriber(int i) {
		return sessionImpl.getTranscriber(i);
	}

	public void removeTranscriber(int i) {
		sessionImpl.removeTranscriber(i);
	}

	public Transcribers getTranscribers() {
		return new Transcribers(this);
	}

	public SessionMetadata getMetadata() {
		return sessionImpl.getMetadata();
	}

	/* Participants */
	public int getParticipantCount() {
		return sessionImpl.getParticipantCount();
	}

	public void addParticipant(Participant participant) {
		sessionImpl.addParticipant(participant);
	}

	public void addParticipant(int idx, Participant participant) {
		sessionImpl.addParticipant(idx, participant);
	}

	public Participant getParticipant(int idx) {
		return sessionImpl.getParticipant(idx);
	}

	public int getParticipantIndex(Participant participant) {
		return sessionImpl.getParticipantIndex(participant);
	}

	public Participants getParticipants() {
		return new Participants(this);
	}

	public void removeParticipant(Participant participant) {
		sessionImpl.removeParticipant(participant);
	}

	public void removeParticipant(int idx) {
		sessionImpl.removeParticipant(idx);
	}

	/**
	 * Return session transcript
	 *
	 * @return session transcript
	 */
	public Transcript getTranscript() {
		return sessionImpl.getTranscript();
	}

	/* Record quick access */

	/**
	 * Return {@link Records} wrapper object allowing for enumeration
	 * of records
	 *
	 * @return records wrapper object
	 */
	public Records getRecords() {
		return new Records(this);
	}

	public void addRecord(Record record) {
		getTranscript().addRecord(record);
	}

	public int getRecordElementIndex(int recordIndex) {
		return getTranscript().getRecordElementIndex(recordIndex);
	}

	public int getRecordElementIndex(Record record) {
		return getTranscript().getRecordElementIndex(record);
	}

	public void addRecord(int recordIndex, Record record) {
		getTranscript().addRecord(recordIndex, record);
	}

	public void removeRecord(Record record) {
		getTranscript().removeRecord(record);
	}

	public void removeRecord(int recordIndex) {
		getTranscript().removeRecord(recordIndex);
	}

	public Record getRecord(int recordIndex) {
		return getTranscript().getRecord(recordIndex);
	}

	public int getRecordCount() {
		return getTranscript().getRecordCount();
	}

	public int getRecordPosition(Record record) {
		return getTranscript().getRecordPosition(record);
	}

	public void setRecordPosition(Record record, int recordIndex) {
		getTranscript().setRecordPosition(record, recordIndex);
	}

}

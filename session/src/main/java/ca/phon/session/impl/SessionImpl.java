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
package ca.phon.session.impl;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.spi.*;

public class SessionImpl implements SessionSPI {
	
	/*
	 * Properties
	 */
	private final AtomicReference<String> corpusRef = new AtomicReference<String>();
	
	private final AtomicReference<String> nameRef = new AtomicReference<String>();
	
	private final AtomicReference<LocalDate> dateRef = new AtomicReference<LocalDate>();
	
	private final AtomicReference<String> langRef = new AtomicReference<String>();
	
	private final AtomicReference<String> mediaRef = new AtomicReference<String>();
	
	private final SessionMetadata metadata;
	
	private final List<Participant> participants =
			Collections.synchronizedList(new ArrayList<Participant>());
	
	private final List<Transcriber> transcribers =
			Collections.synchronizedList(new ArrayList<Transcriber>());
	
	private final List<TierViewItem> tierOrder =
			Collections.synchronizedList(new ArrayList<TierViewItem>());
	
	private final List<TierDescription> userTiers =
			Collections.synchronizedList(new ArrayList<TierDescription>());
	
	private final List<Record> records =
			Collections.synchronizedList(new ArrayList<Record>());
	
	SessionImpl() {
		super();
		metadata = SessionFactory.newFactory().createSessionMetadata();
	}

	@Override
	public String getCorpus() {
		return corpusRef.get();
	}

	@Override
	public String getName() {
		return nameRef.get();
	}

	@Override
	public LocalDate getDate() {
		return dateRef.get();
	}

	@Override
	public String getLanguage() {
		return langRef.get();
	}

	@Override
	public String getMediaLocation() {
		return mediaRef.get();
	}

	@Override
	public List<TierViewItem> getTierView() {
		return Collections.unmodifiableList(this.tierOrder);
	}

	@Override
	public Transcriber getTranscriber(String username) {
		Transcriber retVal = null;
		synchronized(transcribers) {
			for(Transcriber transcriber:this.transcribers) {
				final String tName = transcriber.getUsername();
				if(tName.equals(username)) {
					retVal = transcriber;
					break;
				}
			}
		}
		return retVal;
	}
	
	@Override
	public Transcriber getTranscriber(int i) {
		return transcribers.get(i);
	}
	
	@Override
	public void removeTranscriber(int i) {
		transcribers.remove(i);
	}
	
	@Override
	public SessionMetadata getMetadata() {
		return metadata;
	}

	@Override
	public Record getRecord(int pos) {
		return records.get(pos);
	}

	@Override
	public int getRecordCount() {
		return records.size();
	}

	@Override
	public int getRecordPosition(Record record) {
		return records.indexOf(record);
	}
	
	@Override
	public void setRecordPosition(Record record, int position) {
		int currentPos = getRecordPosition(record);
		if(currentPos >= 0) {
			records.remove(currentPos);
			records.add(position, record);
		}
	}

	@Override
	public int getParticipantCount() {
		return participants.size();
	}

	@Override
	public void addParticipant(Participant participant) {
		if(!participants.contains(participant))
			participants.add(participant);
	}

	@Override
	public void addParticipant(int idx, Participant participant) {
		if(!participants.contains(participant))
			participants.add(idx, participant);
	}

	@Override
	public Participant getParticipant(int idx) {
		return participants.get(idx);
	}

	@Override
	public int getParticipantIndex(Participant participant) {
		return participants.indexOf(participant);
	}
	
	@Override
	public void setCorpus(String corpus) {
		corpusRef.getAndSet(corpus);
	}

	@Override
	public void setName(String name) {
		nameRef.getAndSet(name);
	}

	@Override
	public void setDate(LocalDate date) {
		dateRef.getAndSet(date);
	}

	@Override
	public void setLanguage(String language) {
		langRef.getAndSet(language);
	}

	@Override
	public void setMediaLocation(String mediaLocation) {
		mediaRef.getAndSet(mediaLocation);
	}

	@Override
	public void setTierView(List<TierViewItem> view) {
		tierOrder.clear();
		tierOrder.addAll(view);
//		for(TierViewItem item:view) {
//			// Segment tier has been deprecated, make sure it never gets into our view ordering
//			if(!SystemTierType.Segment.getName().equals(item.getTierName())) {
//				tierOrder.add(item);
//			}
//		}
	}

	@Override
	public void addTranscriber(Transcriber t) {
		transcribers.add(t);
	}

	@Override
	public void removeTranscriber(Transcriber t) {
		transcribers.remove(t);
	}

	@Override
	public void removeTranscriber(String username) {
		final Transcriber t = getTranscriber(username);
		if(t != null) {
			transcribers.remove(t);
		}
	}

	@Override
	public void addRecord(Record record) {
		records.add(record);
	}

	@Override
	public void addRecord(int pos, Record record) {
		records.add(pos, record);
	}

	@Override
	public void removeRecord(Record record) {
		records.remove(record);
	}

	@Override
	public void removeRecord(int pos) {
		records.remove(pos);
	}

	@Override
	public void removeParticipant(Participant participant) {
		participants.remove(participant);
	}

	@Override
	public void removeParticipant(int idx) {
		participants.remove(idx);
	}
	
	@Override
	public int getUserTierCount() {
		return userTiers.size();
	}

	@Override
	public TierDescription getUserTier(int idx) {
		return userTiers.get(idx);
	}

	@Override
	public TierDescription removeUserTier(int idx) {
		return userTiers.remove(idx);
	}

	@Override
	public TierDescription removeUserTier(TierDescription tierDescription) {
		userTiers.remove(tierDescription);
		return tierDescription;
	}

	@Override
	public void addUserTier(TierDescription tierDescription) {
		userTiers.add(tierDescription);
	}

	@Override
	public void addUserTier(int idx, TierDescription tierDescription) {
		userTiers.add(idx, tierDescription);
	}
	
	@Override
	public int getTranscriberCount() {
		return transcribers.size();
	}
}

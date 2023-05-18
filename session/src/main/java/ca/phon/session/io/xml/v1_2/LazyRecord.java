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
package ca.phon.session.io.xml.v1_2;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.io.xml.v12.ParticipantType;
import ca.phon.session.io.xml.v12.RecordType;
import ca.phon.session.spi.RecordSPI;
import ca.phon.util.Language;

import java.util.*;

public final class LazyRecord implements RecordSPI {
	
	private SessionFactory factory;
	
	private Session session;
	
	private RecordType recordElement;
	
	private Record internalRecord;

	public UUID getUuid() {
		if(internalRecord == null) {
			final String idStr = recordElement.getId();
			return UUID.fromString(idStr);
		} else {
			return internalRecord.getUuid();
		}
	}

	public void setUuid(UUID id) {
		loadRecord();
		internalRecord.setUuid(id);
	}

	public Participant getSpeaker() {
		if(internalRecord == null) {
			return findSpeaker();
		} else {
			return internalRecord.getSpeaker();
		}
	}
	
	private Participant findSpeaker() {
		if(recordElement.getSpeaker() != null) {
			final ParticipantType pt = (ParticipantType)recordElement.getSpeaker();
			for(int pIdx = 0; pIdx < session.getParticipantCount(); pIdx++) {
				final Participant participant = session.getParticipant(pIdx);
				if(participant.getName() != null  && participant.getName().equals(pt.getName())) {
					return participant;
				} else if(participant.getId() != null && participant.getId().equals(pt.getId())) {
					return participant;
				}
			}
		}
		return Participant.UNKNOWN;
	}

	public void setSpeaker(Participant participant) {
		loadRecord();
		internalRecord.setSpeaker(participant);
	}

	@Override
	public Language getLanguage() {
		loadRecord();
		return internalRecord.getLanguage();
	}

	@Override
	public void setLanguage(Language language) {
		loadRecord();
		internalRecord.setLanguage(language);
	}

	public MediaSegment getMediaSegment() {
		loadRecord();
		return internalRecord.getMediaSegment();
	}

	public void setMediaSegment(MediaSegment segment) {
		loadRecord();
		internalRecord.setMediaSegment(segment);
	}

	public Tier<MediaSegment> getSegmentTier() {
		loadRecord();
		return internalRecord.getSegmentTier();
	}

	@Override
	public Tier<GroupSegment> getGroupSegment() {
		loadRecord();
		return internalRecord.getGroupSegment();
	}

	public boolean isExcludeFromSearches() {
		loadRecord();
		return internalRecord.isExcludeFromSearches();
	}

	public void setExcludeFromSearches(boolean excluded) {
		loadRecord();
		internalRecord.setExcludeFromSearches(excluded);
	}

	public Tier<Orthography> getOrthographyTier() {
		loadRecord();
		return internalRecord.getOrthographyTier();
	}

	public void setOrthography(Tier<Orthography> ortho) {
		loadRecord();
		internalRecord.setOrthography(ortho);
	}

	public Tier<IPATranscript> getIPATargetTier() {
		loadRecord();
		return internalRecord.getIPATargetTier();
	}

	public void setIPATarget(Tier<IPATranscript> ipa) {
		loadRecord();
		internalRecord.setIPATarget(ipa);
	}

	public Tier<IPATranscript> getIPAActualTier() {
		loadRecord();
		return internalRecord.getIPAActualTier();
	}

	public void setIPAActual(Tier<IPATranscript> ipa) {
		loadRecord();
		internalRecord.setIPAActual(ipa);
	}

	public Tier<PhoneMap> getPhoneAlignmentTier() {
		loadRecord();
		return internalRecord.getPhoneAlignmentTier();
	}

	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		loadRecord();
		internalRecord.setPhoneAlignment(phoneAlignment);
	}

	public Tier<TierString> getNotesTier() {
		loadRecord();
		return internalRecord.getNotesTier();
	}

	public void setNotes(Tier<TierString> notes) {
		loadRecord();
		internalRecord.setNotes(notes);
	}

	public Class<?> getTierType(String name) {
		loadRecord();
		return internalRecord.getTierType(name);
	}

	public <T> Tier<T> getTier(String name, Class<T> type) {
		loadRecord();
		return internalRecord.getTier(name, type);
	}

	public Tier<?> getTier(String name) {
		loadRecord();
		return internalRecord.getTier(name);
	}

	public Set<String> getUserDefinedTierNames() {
		loadRecord();
		return internalRecord.getUserDefinedTierNames();
	}

	public boolean hasTier(String name) {
		loadRecord();
		return internalRecord.hasTier(name);
	}

	public void putTier(Tier<?> tier) {
		loadRecord();
		internalRecord.putTier(tier);
	}

	public void removeTier(String name) {
		loadRecord();
		internalRecord.removeTier(name);
	}

	public int getNumberOfComments() {
		loadRecord();
		return internalRecord.getNumberOfComments();
	}

	public Comment getComment(int idx) {
		loadRecord();
		return internalRecord.getComment(idx);
	}

	public void addComment(Comment comment) {
		loadRecord();
		internalRecord.addComment(comment);
	}

	public void removeComment(Comment comment) {
		loadRecord();
		internalRecord.removeComment(comment);
	}

	public void removeComment(int idx) {
		loadRecord();
		internalRecord.removeComment(idx);
	}

	LazyRecord(SessionFactory factory, Session session, RecordType ele) {
		super();
		this.factory = factory;
		this.session = session;
		this.recordElement = ele;
	}
	
	private void loadRecord() {
		if(internalRecord != null) return;
		final XmlSessionReaderV1_2 reader = new XmlSessionReaderV1_2();
		internalRecord = reader.copyRecord(factory, session, recordElement);
	}

	@Override
	public <T> List<Tier<T>> getTiersOfType(Class<T> type) {
		loadRecord();
		return internalRecord.getTiersOfType(type);
	}

}

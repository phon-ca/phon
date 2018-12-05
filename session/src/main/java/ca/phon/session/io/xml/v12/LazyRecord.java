/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.session.io.xml.v12;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.Comment;
import ca.phon.session.Group;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierString;

public class LazyRecord implements Record {
	
	private SessionFactory factory;
	
	private Session session;
	
	private RecordType recordElement;
	
	private Record internalRecord;
	
	public Set<Class<?>> getExtensions() {
		loadRecord();
		return internalRecord.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		loadRecord();
		return internalRecord.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		loadRecord();
		return internalRecord.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		loadRecord();
		return internalRecord.removeExtension(cap);
	}

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

	public Tier<MediaSegment> getSegment() {
		loadRecord();
		return internalRecord.getSegment();
	}

	public void setSegment(Tier<MediaSegment> media) {
		loadRecord();
		internalRecord.setSegment(media);
	}

	public boolean isExcludeFromSearches() {
		loadRecord();
		return internalRecord.isExcludeFromSearches();
	}

	public void setExcludeFromSearches(boolean excluded) {
		loadRecord();
		internalRecord.setExcludeFromSearches(excluded);
	}

	public Tier<Orthography> getOrthography() {
		loadRecord();
		return internalRecord.getOrthography();
	}

	public void setOrthography(Tier<Orthography> ortho) {
		loadRecord();
		internalRecord.setOrthography(ortho);
	}

	public int numberOfGroups() {
		loadRecord();
		return internalRecord.numberOfGroups();
	}

	public Group getGroup(int idx) {
		loadRecord();
		return internalRecord.getGroup(idx);
	}

	public void removeGroup(int idx) {
		loadRecord();
		internalRecord.removeGroup(idx);
	}

	public Group addGroup() {
		loadRecord();
		return internalRecord.addGroup();
	}

	public Group addGroup(int idx) {
		loadRecord();
		return internalRecord.addGroup(idx);
	}

	public int mergeGroups(int grp1, int grp2) {
		loadRecord();
		return internalRecord.mergeGroups(grp1, grp2);
	}

	public Group splitGroup(int grp, int wrd) {
		loadRecord();
		return internalRecord.splitGroup(grp, wrd);
	}

	public Tier<IPATranscript> getIPATarget() {
		loadRecord();
		return internalRecord.getIPATarget();
	}

	public void setIPATarget(Tier<IPATranscript> ipa) {
		loadRecord();
		internalRecord.setIPATarget(ipa);
	}

	public Tier<IPATranscript> getIPAActual() {
		loadRecord();
		return internalRecord.getIPAActual();
	}

	public void setIPAActual(Tier<IPATranscript> ipa) {
		loadRecord();
		internalRecord.setIPAActual(ipa);
	}

	public Tier<PhoneMap> getPhoneAlignment() {
		loadRecord();
		return internalRecord.getPhoneAlignment();
	}

	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		loadRecord();
		internalRecord.setPhoneAlignment(phoneAlignment);
	}

	public Tier<TierString> getNotes() {
		loadRecord();
		return internalRecord.getNotes();
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

	public Set<String> getExtraTierNames() {
		loadRecord();
		return internalRecord.getExtraTierNames();
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

	public LazyRecord(SessionFactory factory, Session session, RecordType ele) {
		super();
		this.factory = factory;
		this.session = session;
		this.recordElement = ele;
	}
	
	private void loadRecord() {
		if(internalRecord != null) return;
		final XMLSessionReader_v12 reader = new XMLSessionReader_v12();
		internalRecord = reader.copyRecord(factory, session, recordElement);
	}

	@Override
	public <T> List<Tier<T>> getTiersOfType(Class<T> type) {
		loadRecord();
		return internalRecord.getTiersOfType(type);
	}

}

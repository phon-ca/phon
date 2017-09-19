/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session.io.xml.v12;

import java.util.*;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;
import ca.phon.session.*;

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

}

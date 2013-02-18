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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * A wrapper object for another transcript.  This class
 * allows for editing of the original transcript while
 * only displaying records for the given participants.
 * 
 */
public class FilteredTranscript implements ISession {
	
	/** The original transcript */
	private ISession originalTranscript;
	
	/** Allowed participants */
	private List<IParticipant> participants;
	
	/** The list of filtered records */
	private List<IUtterance> filteredUtts;
	
	/** 
	 * Constructor
	 */
	public FilteredTranscript(ISession orig) {
		this(orig, new ArrayList<IParticipant>());
	}
	
	public FilteredTranscript(ISession orig, List<IParticipant> parts) {
		this.originalTranscript = orig;
		this.participants = parts;
		
		filterUtterances();
	}
	
	private void filterUtterances() {
		filteredUtts = new ArrayList<IUtterance>();
		for(IUtterance utt:this.originalTranscript.getUtterances()) {
			IParticipant uttPart = utt.getSpeaker();
			if(uttPart != null) {
				if(participants.contains(uttPart))
					filteredUtts.add(utt);
			}
		}
	}

	@Override
	public String getCorpus() {
		return this.originalTranscript.getCorpus();
	}

	@Override
	public Calendar getDate() {
		return this.originalTranscript.getDate();
	}

	@Override
	public List<IDepTierDesc> getDependentTiers() {
		return this.originalTranscript.getDependentTiers();
	}

	@Override
	public String getID() {
		return this.originalTranscript.getID();
	}

	@Override
	public String getLanguage() {
		return this.originalTranscript.getLanguage();
	}

	@Override
	public String getMediaLocation() {
		return this.originalTranscript.getMediaLocation();
	}

	@Override
	public IMetadata getMetainfo() {
		return this.originalTranscript.getMetainfo();
	}

	@Override
	public int getNumberOfUtterances() {
		return filteredUtts.size();
	}

	@Override
	public List<IParticipant> getParticipants() {
		return this.originalTranscript.getParticipants();
	}

	@Override
	public List<ITierOrderItem> getTierView() {
		return this.originalTranscript.getTierView();
	}

	@Override
	public ITranscriber getTranscriber(String username) {
		return this.originalTranscript.getTranscriber(username);
	}

	@Override
	public List<ITranscriber> getTranscribers() {
		return this.originalTranscript.getTranscribers();
	}

	@Override
	public int getUtteranceIndex(IUtterance utt) {
		int retVal = -1;
		for(int i = 0; i < filteredUtts.size(); i++) {
			IUtterance u = filteredUtts.get(i);
			if(u.getID().equals(utt.getID())) {
				retVal = i;
				break;
			}
		}
		return retVal;
	}

	@Override
	public List<IUtterance> getUtterances() {
		return filteredUtts;
	}

	@Override
	public String getVersion() {
		return this.originalTranscript.getVersion();
	}

	@Override
	public List<IDepTierDesc> getWordAlignedTiers() {
		return this.originalTranscript.getWordAlignedTiers();
	}

	@Override
	public void loadTranscriptData(InputStream in) throws IOException {
		// no-op
	}

	@Override
	public IDepTierDesc newDependentTier() {
		return this.originalTranscript.newDependentTier();
	}

	@Override
	public IParticipant newParticipant() {
		return this.originalTranscript.newParticipant();
	}

	@Override
	public ITranscriber newTranscriber() {
		return this.originalTranscript.newTranscriber();
	}

	@Override
	public IUtterance newUtterance() {
		return this.originalTranscript.newUtterance();
	}

	@Override
	public IUtterance newUtterance(int pos) {
		return this.originalTranscript.newUtterance(pos);
	}

	@Override
	public void removeDependentTier(String tierName) {
		this.originalTranscript.removeDependentTier(tierName);
	}

	@Override
	public void removeParticipant(IParticipant participant) {
		this.removeParticipant(participant);
	}

	@Override
	public void removeTranscriber(ITranscriber t) {
		this.originalTranscript.removeTranscriber(t);
	}

	@Override
	public void removeTranscriber(String username) {
		this.originalTranscript.removeTranscriber(username);
	}

	@Override
	public void removeUtterance(IUtterance utt) {
		this.originalTranscript.removeUtterance(utt);
		this.filteredUtts.remove(utt);
	}

	@Override
	public void saveTranscriptData(OutputStream out) throws IOException {
		// no-op
	}

	@Override
	public void setCorpus(String corpus) {
		this.originalTranscript.setCorpus(corpus);
	}

	@Override
	public void setDate(Calendar date) {
		this.originalTranscript.setDate(date);
	}

	@Override
	public void setID(String id) {
		this.originalTranscript.setID(id);
	}

	@Override
	public void setLanguage(String language) {
		this.originalTranscript.setLanguage(language);
	}

	@Override
	public void setMediaLocation(String mediaLocation) {
		this.originalTranscript.setMediaLocation(mediaLocation);
	}

	@Override
	public void setTierView(List<ITierOrderItem> view) {
		this.originalTranscript.setTierView(view);
	}

	@Override
	public void setVersion(String version) {
		this.originalTranscript.setVersion(version);
	}

	@Override
	public List<TranscriptElement<Object>> getTranscriptElements() {
		return new ArrayList<TranscriptElement<Object>>();
	}

	@Override
	public IComment newComment() {
		return null;
	}

	@Override
	public void sortRecords(Comparator<IUtterance> comp) {
		this.originalTranscript.sortRecords(comp);
	}

	@Override
	public IUtterance getUtterance(int uttindex) {
		return getUtterances().get(uttindex);
	}

	private File cachedFile = null;
	@Override
	public void loadTranscriptFile(File f) throws IOException {
		cachedFile = f;
		FileInputStream fin = new FileInputStream(f);
		loadTranscriptData(fin);
	}

	@Override
	public void saveTranscript() throws IOException {
		if(cachedFile != null) {
			FileOutputStream fout = new FileOutputStream(cachedFile);
			saveTranscriptData(fout);
		} else {
			throw new IOException("No file");
		}
	}

}

/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session.impl;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.Word;

public class GroupImpl implements Group {
	
	/**
	 * Weak reference to parent record
	 */
	private final Record record;

	/**
	 * Group index
	 */
	private final int groupIndex;
	
	GroupImpl(Record record, int idx) {
		super();
		this.record = record;
		this.groupIndex = idx;
	}
	
	public Record getRecord() {
		return this.record;
	}
	
	public int getGroupIndex() {
		return this.groupIndex;
	}
	
	@Override
	public Orthography getOrthography() {
		return 
				(record.getOrthography().numberOfGroups() > groupIndex ? record.getOrthography().getGroup(groupIndex) 
						: null);
	}

	@Override
	public void setOrthography(Orthography ortho) {
		record.getOrthography().setGroup(groupIndex, ortho);
	}

	@Override
	public IPATranscript getIPATarget() {
		return 
				(record.getIPATarget().numberOfGroups() > groupIndex ?  record.getIPATarget().getGroup(groupIndex)
						: new IPATranscript());
	}

	@Override
	public IPATranscript getIPAActual() {
		return 
				(record.getIPAActual().numberOfGroups() > groupIndex ? record.getIPAActual().getGroup(groupIndex)
						: new IPATranscript());
	}
	
	@Override
	public Object getTier(String name) {
		final Tier<?> tier = record.getTier(name);
		if(tier == null) return null;
		int gIdx = groupIndex;
		if(!tier.isGrouped())
			gIdx = 0;
		final Object retVal = 
				(tier.numberOfGroups() > gIdx ? tier.getGroup(gIdx) : null);
		return retVal;
	}

	@Override
	public <T> T getTier(String name, Class<T> type) {
		final Tier<T> tier = record.getTier(name, type);
		if(tier == null) return null;
		int gIdx = groupIndex;
		if(!tier.isGrouped())
			gIdx = 0;
		final T retVal = 
				(tier.numberOfGroups() > gIdx ? tier.getGroup(gIdx) : null);
		return retVal;
	}

	@Override
	public void setIPATarget(IPATranscript ipa) {
		record.getIPATarget().setGroup(groupIndex, ipa);
	}

	@Override
	public void setIPAActual(IPATranscript ipa) {
		record.getIPAActual().setGroup(groupIndex, ipa);
	}

	@Override
	public PhoneMap getPhoneAlignment() {
		final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
		return (alignmentTier != null && 
				alignmentTier.numberOfGroups() > groupIndex ? alignmentTier.getGroup(groupIndex) : null);
	}

	@Override
	public void setPhoneAlignment(PhoneMap alignment) {
		record.getPhoneAlignment().setGroup(groupIndex, alignment);
	}

	@Override
	public String getNotes() {
		return 
				(record.getNotes() != null && 
					record.getNotes().numberOfGroups() > 0 ? record.getNotes().getGroup(0) : null);
	}

	@Override
	public void setNotes(String notes) {
		record.getNotes().setGroup(0, notes);
	}

	@Override
	public <T> void setTier(String name, Class<T> type, T val) {
		final Tier<T> tier = record.getTier(name, type);
		if(tier != null) {
			if(tier.isGrouped())
				tier.setGroup(groupIndex, val);
			else
				tier.setGroup(0, val);
		}
	}
	
	@Override
	public Word getAlignedWord(int wordIndex) {
		return new WordImpl(getRecord(), groupIndex, wordIndex);
	}
	
	@Override
	public int getAlignedWordCount() {
		int retVal = getWordCount(SystemTierType.Orthography.getName());
		retVal = Math.max(retVal, getWordCount(SystemTierType.IPATarget.getName()));
		retVal = Math.max(retVal, getWordCount(SystemTierType.IPAActual.getName()));
		
		for(String tierName:record.getExtraTierNames()) {
			final Tier<String> tier = record.getTier(tierName, String.class);
			if(tier.isGrouped())
				retVal = Math.max(retVal, getWordCount(tierName));
		}
		
		return retVal;
	}
	
	@Override
	public int getWordCount(String tierName) {
		final Object obj = getTier(tierName);
		
		int retVal = 0;
		if(obj instanceof Orthography) {
			final Orthography ortho = (Orthography)obj;
			final OrthoWordExtractor extractor = new OrthoWordExtractor();
			ortho.accept(extractor);
			retVal = extractor.getWordList().size();
		} else if(obj instanceof IPATranscript) {
			retVal = ((IPATranscript)obj).words().size();
		} else if(obj instanceof String) {
			retVal = obj.toString().split("\\p{Space}").length;
		}
		
		return retVal;
	}

}

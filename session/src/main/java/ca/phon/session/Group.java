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
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.*;
import ca.phon.orthography.*;

/**
 * A Group is a vertical view of tier information
 * in a record.
 * 
 */
public final class Group extends ExtendableObject {
	
	private final Record record;

	/**
	 * Group index
	 */
	private final int groupIndex;
	
	Group(Record record, int idx) {
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
	
	public Orthography getOrthography() {
		return 
				(record.getOrthography().numberOfGroups() > groupIndex ? record.getOrthography().getGroup(groupIndex) 
						: new Orthography());
	}

	public void setOrthography(Orthography ortho) {
		record.getOrthography().setGroup(groupIndex, ortho);
	}

	public IPATranscript getIPATarget() {
		return 
				(record.getIPATarget().numberOfGroups() > groupIndex ?  record.getIPATarget().getGroup(groupIndex)
						: new IPATranscript());
	}

	public IPATranscript getIPAActual() {
		return 
				(record.getIPAActual().numberOfGroups() > groupIndex ? record.getIPAActual().getGroup(groupIndex)
						: new IPATranscript());
	}
	
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

	public void setIPATarget(IPATranscript ipa) {
		record.getIPATarget().setGroup(groupIndex, ipa);
	}

	public void setIPAActual(IPATranscript ipa) {
		record.getIPAActual().setGroup(groupIndex, ipa);
	}

	public PhoneMap getPhoneAlignment() {
		final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
		return (alignmentTier != null && 
				alignmentTier.numberOfGroups() > groupIndex ? alignmentTier.getGroup(groupIndex) : null);
	}

	public void setPhoneAlignment(PhoneMap alignment) {
		record.getPhoneAlignment().setGroup(groupIndex, alignment);
	}

	public TierString getNotes() {
		return 
				(record.getNotes() != null && 
					record.getNotes().numberOfGroups() > 0 ? record.getNotes().getGroup(0) : null);
	}

	public void setNotes(TierString notes) {
		record.getNotes().setGroup(0, notes);
	}

	public <T> void setTier(String name, Class<T> type, T val) {
		final Tier<T> tier = record.getTier(name, type);
		if(tier != null) {
			if(tier.isGrouped())
				tier.setGroup(groupIndex, val);
			else
				tier.setGroup(0, val);
		}
	}

	public Word getAlignedWord(int wordIndex) {
		return new Word(getRecord(), groupIndex, wordIndex);
	}
	
	public int getAlignedWordCount() {
		int retVal = getWordCount(SystemTierType.Orthography.getName());
		retVal = Math.max(retVal, getWordCount(SystemTierType.IPATarget.getName()));
		retVal = Math.max(retVal, getWordCount(SystemTierType.IPAActual.getName()));
		retVal = Math.max(retVal, getWordCount(SystemTierType.Segment.getName()));
		
		for(String tierName:record.getExtraTierNames()) {
			final Tier<String> tier = record.getTier(tierName, String.class);
			if(tier.isGrouped())
				retVal = Math.max(retVal, getWordCount(tierName));
		}
		
		return retVal;
	}
	
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
		} else if(obj instanceof TierString) {
			retVal = ((TierString)obj).numberOfWords();
		} else if(obj instanceof String) {
			retVal = obj.toString().split("\\p{Space}").length;
		} else if(obj instanceof GroupSegment) {
			retVal = ((GroupSegment)obj).getWordSegments().size();
		}
		
		return retVal;
	}

	public SyllableMap getSyllableAlignment() {
		final SyllableAligner aligner = new SyllableAligner();
		return aligner.calculateSyllableAlignment(getIPATarget(), getIPAActual(), getPhoneAlignment());
	}

	public int getAlignedSyllableCount() {
		return getSyllableAlignment().getAlignmentLength();
	}
	
	public AlignedSyllable getAlignedSyllable(int index) {
		return new AlignedSyllable(getRecord(), groupIndex, index);
	}
	
}

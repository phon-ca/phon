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

import java.util.concurrent.atomic.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;

public final class AlignedSyllable extends ExtendableObject {
	
	// record
	private final AtomicReference<Record> recordRef;

	// group
	private final int groupIndex;

	// word index
	private final int wordIndex;
	
	// syllable index
	private final int syllIndex;

	AlignedSyllable(Record record, int groupIndex, int syllIndex) {
		super();
		
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = -1;
		this.syllIndex = syllIndex;
	}
	
	AlignedSyllable(Record record, int groupIndex, int wordIndex, int syllIndex) {
		super();
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = wordIndex;
		this.syllIndex = syllIndex;
	}

	public IPATranscript getIPATarget() {
		final SyllableMap syllableAlignment = 
				(getWordIndex() >= 0 ? getWord().getSyllableAlignment() : getGroup().getSyllableAlignment());
		if(getSyllableIndex() < syllableAlignment.getAlignmentLength()) {
			return syllableAlignment.getTopAlignmentElements().get(getSyllableIndex());
		} else {
			return null;
		}
	}

	public IPATranscript getIPAActual() {
		final SyllableMap syllableAlignment = 
				(getWordIndex() >= 0 ? getWord().getSyllableAlignment() : getGroup().getSyllableAlignment());
		if(getSyllableIndex() < syllableAlignment.getAlignmentLength()) {
			return syllableAlignment.getBottomAlignmentElements().get(getSyllableIndex());
		} else {
			return null;
		}
	}

	public int getIPATargetLocation() {
		int retVal = -1;
		
		final IPATranscript target = getGroup().getIPATarget();
		if(target != null) {
			final IPATranscript ipa = getIPATarget();
		
			if(ipa != null) {
				final int eleIdx = target.indexOf(ipa);
				retVal = target.stringIndexOfElement(eleIdx);
			}
		}
		
		return retVal;
	}
	
	public int getIPAActualLocation() {
		int retVal = -1;
		
		final IPATranscript actual = getGroup().getIPAActual();
		if(actual != null) {
			final IPATranscript ipa = getIPAActual();
		
			if(ipa != null) {
				final int eleIdx = actual.indexOf(ipa);
				retVal = actual.stringIndexOfElement(eleIdx);
			}
		}
		
		return retVal;
	}

	public PhoneMap getPhoneAlignment() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) new PhoneMap();

		return grpAlignment.getSubAlignment(ipaT, ipaA);
	}
	
	public int getPhoneAlignmentLocation() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) return -1;
		
		return grpAlignment.getSubAlignmentIndex(ipaT, ipaA);
	}

	public Group getGroup() {
		final Record record = recordRef.get();
		if(record != null) {
			return record.getGroup(groupIndex);
		} else {
			return null;
		}
	}

	public Word getWord() {
		return new Word(recordRef.get(), groupIndex, wordIndex);
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public int getSyllableIndex() {
		return syllIndex;
	}

	public int getGroupIndex() {
		return groupIndex;
	}
	
}

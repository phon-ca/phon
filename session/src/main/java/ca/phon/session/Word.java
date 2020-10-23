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
package ca.phon.session;

import java.util.*;
import java.util.concurrent.atomic.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.orthography.*;

/**
 * <p>Aligned word access for a record. A word is typically any text content
 * between spaces unless otherwise specified by the specific tier
 * implementation.  The class allows for vertical access of
 * a word in each tier.</p>
 * 
 * <p>Words can only be obtained through {@link Group} objects.<br/>
 * <pre>
 * Group g = record.getGroup(0);
 * Word w = g.getWord(1);
 * 
 * // these 'word' elements are aligned
 * OrthoElement ele = record.getOrthography();
 * IPATranscript ipaA = record.getIPAActual();
 * 
 * </pre>
 * </p>
 * 
 * 
 */
public final class Word extends ExtendableObject {

	// record
	private final AtomicReference<Record> recordRef;

	// group
	private final int groupIndex;

	// word index
	private final int wordIndex;

	Word(Record record, int groupIndex, int wordIndex) {
		super();
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = wordIndex;
	}

	public Group getGroup() {
		final Record record = recordRef.get();
		if(record != null) {
			return record.getGroup(groupIndex);
		} else {
			return null;
		}
	}

	public int getWordIndex() {
		return this.wordIndex;
	}

	public OrthoElement getOrthography() {
		final Orthography ortho =
				(getGroup().getOrthography() == null ? new Orthography() : getGroup().getOrthography());
		final OrthoWordExtractor extractor = new OrthoWordExtractor();
		ortho.accept(extractor);

		final List<OrthoElement> wordList = extractor.getWordList();

		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}

	public int getOrthographyWordLocation() {
		int retVal = -1;

		final OrthoElement ele = getOrthography();
		if(ele != null) {
			final int idx = getGroup().getOrthography().indexOf(ele);
			if(idx >= 0) {
				retVal = 0;
				for(int i = 0; i < idx; i++) {
					retVal += (i > 0 ? 1 : 0) + getGroup().getOrthography().elementAt(i).toString().length();
				}
				if(idx > 0) retVal++;
			}
		}

		return retVal;
	}

	public IPATranscript getIPATarget() {
		final IPATranscript ipaTarget =
				(getGroup().getIPATarget() == null ? new IPATranscript() : getGroup().getIPATarget());
		final List<IPATranscript> wordList = ipaTarget.words();

		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}

	public int getIPATargetWordLocation() {
		int retVal = -1;

		final IPATranscript target = getGroup().getIPATarget();
		final IPATranscript ipa = getIPATarget();
		if(ipa != null) {
			final int eleIdx = target.indexOf(ipa);
			retVal = target.stringIndexOfElement(eleIdx);
		}

		return retVal;
	}

	public IPATranscript getIPAActual() {
		final IPATranscript ipaActual =
				(getGroup().getIPAActual() == null ? new IPATranscript() : getGroup().getIPAActual());
		final List<IPATranscript> wordList = ipaActual.words();

		if(wordIndex >= 0 && wordIndex < wordList.size()) {
			return wordList.get(wordIndex);
		} else {
			return null;
		}
	}

	public int getIPAActualWordLocation() {
		int retVal = -1;

		final IPATranscript actual = getGroup().getIPAActual();
		final IPATranscript ipa = getIPAActual();
		if(ipa != null) {
			final int eleIdx = actual.indexOf(ipa);
			retVal = actual.stringIndexOfElement(eleIdx);
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

	public TierString getNotes() {
		final TierString notes = getGroup().getNotes();

		if(notes != null && wordIndex >= 0 && wordIndex < notes.numberOfWords()) {
			return notes.getWord(wordIndex);
		} else {
			return null;
		}
	}

	public int getNotesWordLocation() {
		int retVal = -1;

		final TierString notes = getGroup().getNotes();
		final String[] wordList = notes.split("\\p{Space}");

		if(wordIndex >=0 && wordIndex < wordList.length) {
			int currentIdx = 0;
			for(int i = 0; i < wordIndex; i++) {
				currentIdx += (i > 0 ? 1 : 0) + wordList[i].length();
			}
			retVal = currentIdx;
		}

		return retVal;
	}

	public Object getTier(String name) {
		Object retVal = null;

		// check for system tier
		final SystemTierType systemTier = SystemTierType.tierFromString(name);
		if(systemTier != null) {
			switch(systemTier) {
			case Orthography:
				retVal = getOrthography();
				break;

			case IPATarget:
				retVal = getIPATarget();
				break;

			case IPAActual:
				retVal = getIPAActual();
				break;

			case Notes:
				retVal = getNotes();
				break;

			default:
				break;
			}
		}

		if(retVal == null) {
			final TierString tierValue = getGroup().getTier(name, TierString.class);

			if(tierValue != null && wordIndex >= 0 && wordIndex < tierValue.numberOfWords())
				retVal = tierValue.getWord(wordIndex);
		}

		return retVal;
	}

	public int getTierWordLocation(String tierName) {
		int retVal = -1;

		final TierString tierString = getGroup().getTier(tierName, TierString.class);

		if(wordIndex >= 0 && wordIndex < tierString.numberOfWords()) {
			retVal = tierString.getWordOffset(wordIndex);
		}

		return retVal;
	}

	public SyllableMap getSyllableAlignment() {
		final SyllableAligner aligner = new SyllableAligner();
		return aligner.calculateSyllableAlignment(getIPATarget(), getIPAActual(), getPhoneAlignment());
	}

	public int getSyllableAlignmentLocation() {
		return getPhoneAlignmentLocation();
	}

	public int getAlignedSyllableCount() {
		return getSyllableAlignment().getAlignmentLength();
	}

	public AlignedSyllable getAlignedSyllable(int index) {
		return new AlignedSyllable(recordRef.get(), groupIndex, wordIndex, index);
	}
	
}

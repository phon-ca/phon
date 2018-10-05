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
package ca.phon.session.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.alignment.SyllableAligner;
import ca.phon.ipa.alignment.SyllableMap;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWordExtractor;
import ca.phon.orthography.Orthography;
import ca.phon.session.AlignedSyllable;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierString;
import ca.phon.session.Word;

public class WordImpl implements Word {

	// record
	private final AtomicReference<Record> recordRef;

	// group
	private final int groupIndex;

	// word index
	private final int wordIndex;

	public WordImpl(Record record, int groupIndex, int wordIndex) {
		super();
		this.recordRef = new AtomicReference<Record>(record);
		this.groupIndex = groupIndex;
		this.wordIndex = wordIndex;
	}

	@Override
	public Group getGroup() {
		final Record record = recordRef.get();
		if(record != null) {
			return record.getGroup(groupIndex);
		} else {
			return null;
		}
	}

	@Override
	public int getWordIndex() {
		return this.wordIndex;
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
	public PhoneMap getPhoneAlignment() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) new PhoneMap();

		return grpAlignment.getSubAlignment(ipaT, ipaA);
	}
	
	@Override
	public int getPhoneAlignmentLocation() {
		final IPATranscript ipaT = (getIPATarget() == null ? new IPATranscript() : getIPATarget());
		final IPATranscript ipaA = (getIPAActual() == null ? new IPATranscript() : getIPAActual());

		final PhoneMap grpAlignment = getGroup().getPhoneAlignment();
		if(grpAlignment == null) return -1;
		
		return grpAlignment.getSubAlignmentIndex(ipaT, ipaA);
	}

	@Override
	public TierString getNotes() {
		final TierString notes = getGroup().getNotes();

		if(wordIndex >= 0 && wordIndex < notes.numberOfWords()) {
			return notes.getWord(wordIndex);
		} else {
			return null;
		}
	}

	@Override
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

	@Override
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

	@Override
	public int getTierWordLocation(String tierName) {
		int retVal = -1;

		final TierString tierString = getGroup().getTier(tierName, TierString.class);

		if(wordIndex >= 0 && wordIndex < tierString.numberOfWords()) {
			retVal = tierString.getWordOffset(wordIndex);
		}

		return retVal;
	}

	@Override
	public SyllableMap getSyllableAlignment() {
		final SyllableAligner aligner = new SyllableAligner();
		return aligner.calculateSyllableAlignment(getIPATarget(), getIPAActual(), getPhoneAlignment());
	}

	@Override
	public int getSyllableAlignmentLocation() {
		return getPhoneAlignmentLocation();
	}

	@Override
	public int getAlignedSyllableCount() {
		return getSyllableAlignment().getAlignmentLength();
	}

	@Override
	public AlignedSyllable getAlignedSyllable(int index) {
		return new AlignedSyllableImpl(recordRef.get(), groupIndex, wordIndex, index);
	}
}

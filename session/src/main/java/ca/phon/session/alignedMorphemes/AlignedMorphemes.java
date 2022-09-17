/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.session.alignedMorphemes;

import ca.phon.extensions.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.OrthoElement;
import ca.phon.session.*;

import java.lang.ref.WeakReference;

/**
 * Extends the aligned word class with the aligned moprheme concept.
 *
 */
@Extension(Word.class)
public class AlignedMorphemes implements ExtensionProvider {

	private WeakReference<Word> alignedWord;

	public AlignedMorphemes() {
		super();
	}

	/**
	 * @return number of morpheme positions in the aligned word
	 */
	public int getMorphemeCount() {
		int retVal = 0;
		final Word alignedWord = getAlignedWord();
		final MorphemeParser morphemeParser = new MorphemeParser();
		if(alignedWord != null) {
			OrthoElement orthoWord = alignedWord.getOrthography();
			OrthoElement[] orthoMorphemes = orthoWord == null ? new OrthoElement[0] : morphemeParser.parseOrthography(alignedWord.getOrthography());
			retVal = Math.max(retVal, orthoMorphemes.length);

			IPATranscript ipaTarget = alignedWord.getIPATarget();
			IPATranscript[] ipaTargets = ipaTarget == null ? new IPATranscript[0] : morphemeParser.parseIPA(alignedWord.getIPATarget());
			retVal = Math.max(retVal, ipaTargets.length);

			IPATranscript ipaActual = alignedWord.getIPAActual();
			IPATranscript[] ipaActuals = ipaActual == null ? new IPATranscript[0] : morphemeParser.parseIPA(alignedWord.getIPAActual());
			retVal = Math.max(retVal, ipaActuals.length);

			for(String userTierName:alignedWord.getGroup().getRecord().getExtraTierNames()) {
				TierString tierVal = (TierString)alignedWord.getTier(userTierName);
				TierString[] tierMorphemes = tierVal == null ? new TierString[0] : morphemeParser.parseTier((TierString)alignedWord.getTier(userTierName));
				retVal = Math.max(retVal, tierMorphemes.length);
			}
		}
		return retVal;
	}

	public Word getAlignedWord() {
		return this.alignedWord.get();
	}

	public void setAlignedWord(Word alignedWord) {
		this.alignedWord = new WeakReference<>(alignedWord);
	}

	/**
	 * Return aligned morpheme at given index
	 *
	 * @param morphemeIdx
	 * @return aligned morpheme for index
	 *
	 * @throws NullPointerException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public AlignedMorpheme getAlignedMorpheme(int morphemeIdx) {
		if(morphemeIdx < 0 || morphemeIdx >= getMorphemeCount())
			throw new ArrayIndexOutOfBoundsException("Invalid morpheme index " + morphemeIdx);
		if(getAlignedWord() == null)
			throw new NullPointerException("alignedWord");
		return new AlignedMorpheme(getAlignedWord(), morphemeIdx);
	}

	@Override
	public void installExtension(IExtendable obj) {
		setAlignedWord((Word)obj);
		((Word)obj).putExtension(AlignedMorphemes.class, this);
	}

}

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
package ca.phon.phonex;

import java.util.*;

import ca.phon.fsa.*;
import ca.phon.ipa.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * Used to detect syllable boundaries.  Syllable boundaries
 * can be 'hard' (i.e., a '.' or other punctuation) or 'soft'
 * as detected between a (coda, onset) pair.
 *
 */
public class SyllableBoundaryTransition extends PhonexTransition {

	/**
	 * Length of tape matched
	 */
	private int matchLength = 0;

	public SyllableBoundaryTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		boolean retVal = false;
		matchLength = 0;

		final IPATranscript transcript = new IPATranscript(currentState.getTape());
		final List<IPATranscript> sylls = transcript.syllables();

		int tapeIdx = -1;
		if(getOffsetType() == OffsetType.NORMAL) {
			if(currentState.getTapeIndex() >= currentState.getTape().length) return true;
			tapeIdx = currentState.getTapeIndex();
		} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
			if(tapeIdx < 0) return true;
		} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
			if(tapeIdx >= currentState.getTape().length) return true;
		}

		// edges
//		if(tapeIdx == 0 ||
//				tapeIdx == currentState.getTape().length) {
//			retVal = true;
//			matchLength = 0;
//		} else {
			final IPAElement p = currentState.getTape()[tapeIdx];
		// punctuation
			final PunctuationTest test = new PunctuationTest();
			p.accept(test);
			retVal = test.isPunct;
			if(retVal) {
				matchLength = 1;
			}

		// implicit syllable edges
			if(!retVal) {
				for(IPATranscript syll:sylls) {
					if(p == syll.elementAt(0) && (getOffsetType() == OffsetType.NORMAL || getOffsetType() == OffsetType.LOOK_AHEAD)) {
						retVal = true;
						matchLength = 0;
					} else if(p == syll.elementAt(syll.length()-1) && getOffsetType() == OffsetType.LOOK_BEHIND) {
						retVal = true;
						matchLength = 0;
					}
				}
			}
//		}

		return retVal;
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}

	@Override
	public String getImage() {
		return "\\S";
	}

	@Override
	public Object clone() {
		SyllableBoundaryTransition retVal = new SyllableBoundaryTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.setType(getType());
		retVal.setOffsetType(getOffsetType());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		return retVal;
	}

	/**
	 * Class for testing if a phone is punctuation.
	 *
	 */
	public class PunctuationTest extends VisitorAdapter<IPAElement> {

		private boolean isPunct = true;

		@Override
		public void fallbackVisit(IPAElement obj) {
		}

		@Visits
		public void visitPhone(Phone p) {
			isPunct = false;
		}

		@Visits
		public void visitCompoundPhone(CompoundPhone cp) {
			isPunct = false;
		}

	}
}

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

import ca.phon.fsa.*;
import ca.phon.ipa.*;

/**
 * Transition for matching word boundaries.
 *
 */
public class WordBoundaryTransition extends PhonexTransition {

	/**
	 * match length - 0 if at beginning/end of input, 1 if matching a space
	 * set when calling 'follow' and it matches
	 */
	private int matchLength = 0;

	public WordBoundaryTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		boolean retVal = false;

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

		if( (tapeIdx == 0 && currentState.getCurrentState().equals("q0")) ||
				(currentState.getTapeIndex() >= currentState.getTape().length) ) {
			retVal = true;
			matchLength = 0;
		} else {
			IPAElement p = currentState.getTape()[tapeIdx];
			if(p.getText().equals(" ")) {
				retVal = true;
				matchLength = 1;
			}
		}

		return retVal;
	}

	@Override
	public int getMatchLength() {
		return matchLength;
	}

	@Override
	public String getImage() {
		return "\\b";
	}

	@Override
	public Object clone() {
		WordBoundaryTransition retVal = new WordBoundaryTransition();
		FSATransition.copyTransitionInfo(this, retVal);
		return retVal;
	}

}

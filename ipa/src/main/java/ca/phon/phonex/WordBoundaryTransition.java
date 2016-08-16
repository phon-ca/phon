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
package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.OffsetType;
import ca.phon.ipa.IPAElement;

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
			if(currentState.getTapeIndex() >= currentState.getTape().length) return false;
			tapeIdx = currentState.getTapeIndex();
		} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
			if(tapeIdx < 0) return false;
		} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
			if(tapeIdx >= currentState.getTape().length) return false;
		}
		
		if(tapeIdx == 0 && currentState.getCurrentState().equals("q0") ||
				currentState.getTapeIndex() == currentState.getTape().length) {
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
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}

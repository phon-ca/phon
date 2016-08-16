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
import ca.phon.fsa.TransitionType;
import ca.phon.ipa.IPAElement;

/**
 * Transition for matching end of tape input.
 */
public class EndOfInputTransition extends PhonexTransition {
	
	public EndOfInputTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			return (currentState.getTapeIndex() + currentState.getLookAheadOffset() >= 
					currentState.getTape().length);
		} else {
			return currentState.getTapeIndex() == currentState.getTape().length;
		}
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public String getImage() {
		return "$";
	}
	
	@Override
	public Object clone() {
		EndOfInputTransition retVal = new EndOfInputTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}

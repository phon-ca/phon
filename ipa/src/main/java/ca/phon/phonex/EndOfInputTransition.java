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

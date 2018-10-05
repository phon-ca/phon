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
package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.OffsetType;
import ca.phon.ipa.IPAElement;

/**
 * Transition for matching beginning of tape input.
 */
public class BeginningOfInputTransition extends PhonexTransition {
	
	public BeginningOfInputTransition() {
		super(null);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			return ((currentState.getTapeIndex() - (currentState.getLookBehindOffset()-1)) == 0);
		} else {
			return (currentState.getTapeIndex() == 0);
		}
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public String getImage() {
		return "^";
	}
	
	@Override
	public Object clone() {
		BeginningOfInputTransition retVal = new BeginningOfInputTransition();
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		return retVal;
	}

}

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
import ca.phon.ipa.IPAElement;

public class EmptyTransition extends PhonexTransition {

	public EmptyTransition() {
		super(null);
	}

	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		return true;
	}

	@Override
	public String getImage() {
		return "\u03b5";
	}

	@Override
	public int getMatchLength() {
		return 0;
	}
	
	@Override
	public Object clone() {
		EmptyTransition retVal = new EmptyTransition();
		FSATransition.copyTransitionInfo(this, retVal);
		return retVal;
	}

}

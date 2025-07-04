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
package ca.phon.stresspattern.fsa;

import ca.phon.fsa.*;
import ca.phon.stresspattern.StressMatcherType;

public class StressMatcherTransition extends FSATransition<StressMatcherType> {

	/** The type to match */
	private StressMatcherType matchType = StressMatcherType.DontCare;
	
	public StressMatcherTransition(StressMatcherType mType) {
		super();
		super.setType(TransitionType.GREEDY);
		this.matchType = mType;
	}

	public StressMatcherType getMatchType() {
		return matchType;
	}

	public void setMatchType(StressMatcherType matchType) {
		this.matchType = matchType;
	}

	@Override
	public boolean follow(FSAState<StressMatcherType> currentState) {
		if(currentState.getTapeIndex() >= currentState.getTape().length)
			return false;
		else
			return matchType.matches(currentState.getTape()[currentState.getTapeIndex()]);
	}
	
	@Override
	public String getImage() {
		return matchType.name();
	}

}

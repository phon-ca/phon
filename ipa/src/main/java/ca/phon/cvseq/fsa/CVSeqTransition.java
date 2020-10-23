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
package ca.phon.cvseq.fsa;

import ca.phon.cvseq.*;
import ca.phon.fsa.*;

/**
 *
 */
public class CVSeqTransition extends FSATransition<CVSeqType> {
	
	/** The type used to match */
	private CVSeqType matcherType;
	
	public CVSeqTransition(CVSeqType matcher) {
		super();
		
		this.matcherType = matcher;
	}

	/**
	 * @return the matcherType
	 */
	public CVSeqType getMatcherType() {
		return matcherType;
	}

	/**
	 * @param matcherType the matcherType to set
	 */
	public void setMatcherType(CVSeqType matcherType) {
		this.matcherType = matcherType;
	}

	@Override
	public boolean follow(FSAState<CVSeqType> currentState) {
		if(currentState.getTapeIndex() >= currentState.getTape().length)
			return false;
		else 
			return matcherType.matches(currentState.getTape()[currentState.getTapeIndex()]);
	}
	
}

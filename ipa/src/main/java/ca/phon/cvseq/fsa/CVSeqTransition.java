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
package ca.phon.cvseq.fsa;

import ca.phon.cvseq.CVSeqType;
import ca.phon.fsa.FSAState;
import ca.phon.fsa.FSATransition;

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

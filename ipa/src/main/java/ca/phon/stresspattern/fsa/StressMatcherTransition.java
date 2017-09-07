/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

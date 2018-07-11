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
package ca.phon.phonex;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.OffsetType;
import ca.phon.ipa.IPAElement;

public class BackReferenceTransition extends PhonexTransition {
	
	/**
	 * Group index
	 */
	private int groupIndex = 0;
	
	/**
	 * Length of last group matched when
	 * {@link #follow(FSAState)} returned
	 * <code>true</code>
	 * 
	 */
	public int matchLength = 0;
	
	/**
	 * Constructor
	 * 
	 * @param groupIndex
	 * @param secondaryMatchers
	 */
	public BackReferenceTransition(int groupIndex, PhoneMatcher ... secondaryMatchers) {
		super(null, secondaryMatchers);
		this.groupIndex = groupIndex;
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState)  {
		boolean retVal = false;
		
		IPAElement[] groupVal = currentState.getGroup(groupIndex);
		if(groupVal != null && groupVal.length > 0) {
			retVal = true;
			int pIdx = currentState.getTapeIndex();
			for(int i = 0; i < groupVal.length; i++) {
				IPAElement groupPhone = groupVal[i];
				
				// make sure there is enough input left on the tape
				if(pIdx+1 >= currentState.getTape().length) {
					retVal = false;
					break;
				}
	
				IPAElement tapePhone = null;
				
				if(getOffsetType() == OffsetType.NORMAL) {
					tapePhone = currentState.getTape()[pIdx+i];
				} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
					int tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
					if(tapeIdx < 0) {
						retVal = false;
						break;
					}
					tapePhone = currentState.getTape()[tapeIdx];
				} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
					int tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
					if(tapeIdx >= currentState.getTape().length) {
						retVal = false;
						break;
					}
					tapePhone = currentState.getTape()[tapeIdx];
				}
				
				retVal &= tapePhone.getText().equals(groupPhone.getText());
				
				// check plug-in matchers
				for(PhoneMatcher pm:getSecondaryMatchers()) {
					retVal &= pm.matches(tapePhone);
				}
				
			}
			if(retVal) {
				matchLength = groupVal.length;
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
		String retVal = "\\" + groupIndex;
		for(PhoneMatcher pm:getSecondaryMatchers()) {
			retVal += ":" + pm.toString();
		}
		return retVal;
	}
	
	@Override
	public Object clone() {
		BackReferenceTransition retVal = new BackReferenceTransition(groupIndex, getSecondaryMatchers().toArray(new PhoneMatcher[0]));
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		return retVal;
	}
}

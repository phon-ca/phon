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
				if(pIdx+i >= currentState.getTape().length) {
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

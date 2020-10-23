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

import java.util.*;

import ca.phon.fsa.*;
import ca.phon.ipa.*;

/**
 * Transitions using {@link PhoneMatcher}s.
 * 
 */
public class PhonexTransition extends FSATransition<IPAElement> implements Cloneable {
	
	/**
	 * Base matcher
	 */
	private PhoneMatcher baseMatcher;
	
	/**
	 * Syllabification matcher
	 */
	private List<PhoneMatcher> secondaryMatchers = 
			new ArrayList<PhoneMatcher>();
	
	/**
	 * Create a new transition with
	 * the given base phone matcher
	 *
	 * @param matcher
	 */
	public PhonexTransition(PhoneMatcher matcher) {
		this(matcher, new PhoneMatcher[0]);
	}
	
	/**
	 * Create a new transitions with the given
	 * base matcher and secondary matchers
	 * 
	 * @param matcher
	 * @param secondaryMatchers
	 */
	public PhonexTransition(PhoneMatcher matcher, PhoneMatcher ... secondaryMatchers) {
		this.baseMatcher = matcher;
		if(secondaryMatchers != null) {
			for(PhoneMatcher secondaryMatcher:secondaryMatchers) {
				if(secondaryMatcher != null)
					this.secondaryMatchers.add(secondaryMatcher);
			}
		}
	}
	
	/**
	 * Get the base phone matcher
	 * 
	 * @return the base phone matcher
	 */
	public PhoneMatcher getMatcher() {
		return this.baseMatcher;
	}
	
	/**
	 * Get the secondary matchers
	 * 
	 * @return the list secondary {@link PhoneMatcher} matcher
	 */
	public List<PhoneMatcher> getSecondaryMatchers() {
		return this.secondaryMatchers;
	}
	
	/**
	 * Set the type matcher
	 * 
	 * @param typeMatcher
	 */
	public void addSecondaryMatcher(PhoneMatcher typeMatcher) {
		this.secondaryMatchers.add(typeMatcher);
	}
	
	/**
	 * Remove secondary matcher
	 * 
	 * @param matcher
	 */
	public void removeSecondaryMatcher(PhoneMatcher matcher) {
		this.secondaryMatchers.remove(matcher);
	}
	
	@Override
	public boolean follow(FSAState<IPAElement> currentState) {
		int tapeIdx = -1;
		if(getOffsetType() == OffsetType.NORMAL) {
			if(currentState.getTapeIndex() >= currentState.getTape().length) return false;
			tapeIdx = currentState.getTapeIndex();
		} else if(getOffsetType() == OffsetType.LOOK_BEHIND) {
			tapeIdx = currentState.getTapeIndex() - currentState.getLookBehindOffset();
			if(tapeIdx < 0) return false;
		} else if(getOffsetType() == OffsetType.LOOK_AHEAD) {
			tapeIdx = currentState.getTapeIndex() + currentState.getLookAheadOffset();
			if(tapeIdx >= currentState.getTape().length) return false;
		}
		IPAElement obj = currentState.getTape()[tapeIdx];
		boolean retVal = 
				baseMatcher.matches(obj);
		
		for(PhoneMatcher matcher:secondaryMatchers) {
			retVal &= matcher.matches(obj);
		}
		
		return retVal;
	}

	@Override
	public String getImage() {
		String retVal = baseMatcher.toString();
		for(PhoneMatcher pm:secondaryMatchers) {
			retVal += ":" + pm.toString();
		}
		if(getType() != TransitionType.NORMAL) {
			retVal += " (" + getType() + ")";
		}
		if(getOffsetType() != OffsetType.NORMAL) {
			retVal += " (" + getOffsetType() + ")";
		}
		return retVal;
	}
	
	@Override
	public Object clone() {
		PhonexTransition retVal = new PhonexTransition(baseMatcher, secondaryMatchers.toArray(new PhoneMatcher[0]));
		retVal.setFirstState(getFirstState());
		retVal.setToState(getToState());
		retVal.getInitGroups().addAll(getInitGroups());
		retVal.getMatcherGroups().addAll(getMatcherGroups());
		retVal.setType(getType());
		retVal.setOffsetType(getOffsetType());
		return retVal;
	}
	
}

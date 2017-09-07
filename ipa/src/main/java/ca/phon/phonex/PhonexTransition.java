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

import java.util.*;

import ca.phon.fsa.*;
import ca.phon.ipa.IPAElement;

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

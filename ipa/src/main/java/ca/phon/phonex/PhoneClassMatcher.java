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

import ca.phon.ipa.*;

/**
 * Combine multiple phone matchers.
 *
 */
public class PhoneClassMatcher implements PhoneMatcher {
	
	/**
	 * List of matchers
	 */
	private List<PhoneMatcher> matchers =
			new ArrayList<PhoneMatcher>();
	
	private boolean not = false;

	public PhoneClassMatcher() {
		this(false, new PhoneMatcher[0]);
	}
	
	/**
	 * Constructor
	 */
	public PhoneClassMatcher(PhoneMatcher ... matchers) {
		this(false, matchers);
	}
	
	/**
	 * Constructor
	 * 
	 * @param matchers list of matchers
	 */
	public PhoneClassMatcher(boolean not, PhoneMatcher ... matchers) {
		for(PhoneMatcher matcher:matchers) {
			this.matchers.add(matcher);
		}
	}

	public void addMatcher(PhoneMatcher pm) {
		this.matchers.add(pm);
	}
	
	public boolean isNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = not;
		
		for(PhoneMatcher matcher:matchers) {
			if(not)
				retVal &= !matcher.matches(p);
			else
				retVal |= matcher.matches(p);
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		if(isNot())
			builder.append('^');
		for(PhoneMatcher matcher:matchers) {
			builder.append(matcher.toString());
		}
		builder.append(']');
		return builder.toString();
	}
}

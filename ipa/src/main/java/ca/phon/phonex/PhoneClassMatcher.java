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
package ca.phon.phonex;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;

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
	 * @param ... list of matchers
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

}

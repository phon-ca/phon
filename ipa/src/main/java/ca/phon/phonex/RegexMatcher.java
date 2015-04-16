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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.ipa.IPAElement;

/**
 * Match phone string against a regular expression.
 * 
 */
public class RegexMatcher implements PhoneMatcher {
	
	/**
	 * Pattern
	 */
	private Pattern pattern;
	
	/**
	 * Create a new matcher for the
	 * given regular expression.
	 * 
	 * @param regex
	 */
	public RegexMatcher(String regex) {
		pattern = Pattern.compile(regex);
	}

	@Override
	public boolean matches(IPAElement p) {
		Matcher m = pattern.matcher(p.getText());
		return m.matches();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
	@Override
	public String toString() {
		return pattern.pattern();
	}

}

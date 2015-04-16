/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.phonex.plugins;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;

/**
 * <p>Provides a matcher for prefix diacritics.  Takes a single
 * argument which is a string list of allowed/forbidden diacritics.
 * Forbidden diacritics are prefixed with '-'.</p>
 * 
 * <p>Usage: <code>comb("&lt;list of diacritics&gt;")</code></br/>
 * E.g., Look for a consonant that has the '&#2045;' diacritic but <em>not</em>
 * the '&#2048;' diacritic.</br>
 * <pre>
 * \c:prefix("&#2045;-&#2048;")
 * </pre>
 * 
 * </p>
 *
 */
@PhonexPlugin(name="prefix")
public class PrefixDiacriticPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args == null)
			throw new NullPointerException();
		if(args.size() != 1) {
			throw new IllegalArgumentException();
		}
		
		final String arg = args.get(0);
		return new PrefixDiacriticPhoneMatcher(arg);
	}

}

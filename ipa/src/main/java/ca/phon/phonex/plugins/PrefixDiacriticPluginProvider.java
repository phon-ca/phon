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
package ca.phon.phonex.plugins;

import ca.phon.phonex.*;

import java.util.List;

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
@PhonexPlugin(name="prefix", description="Match prefix diacritics", arguments={"diacritics"})
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

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

import java.util.List;

/**
 * <p>Extension point for the phonex language.  To add a new plug-in matcher
 * for the phonex langauge, perform the following steps:
 * <ul>
 * <li>Create a type implementing this interface</li>
 * <li>Add to the new type an annotation '@PhonexPlugin("&lt;name&gt;")', where
 * name is the identifier for the plug-in matcher.</li>
 * <li>Create (or modifiy) a file META-INF/services/ca.phon.phonex.PluginProvider with a
 * line containing the full name of the new type.</li>
 * </p>
 * 
 * <p>For an example, see {@link SyllabificationPhonexPlugin}.</p>
 */
public interface PluginProvider {
	
	/**
	 * Create a new matcher for the given input string.
	 * 
	 * @args arguments to the matcher
	 * @return PhoneMatcher
	 * @throws IllegalArgumentException if there was a problem
	 *  creating the plug-in matcher
	 * @throws NullPointerException if the provided argument list
	 *  is <code>null</code>
	 */
	public PhoneMatcher createMatcher(List<String> args)
		throws IllegalArgumentException;

}

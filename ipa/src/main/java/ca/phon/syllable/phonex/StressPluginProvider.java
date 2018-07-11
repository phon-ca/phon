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
package ca.phon.syllable.phonex;

import java.util.List;

import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.PhonexPlugin;
import ca.phon.phonex.PluginProvider;
import ca.phon.syllable.SyllableStress;

/**
 * <p>Provides the plug-in matcher for syllable stress. Stress type
 * is identified by the following list:<br/>
 * 
 * <ul>
 *	<li>U - No Stress</li>
 *  <li>1 - Primary Stress</li>
 *  <li>2 - Secondary Stress</li>
 *  <li>S - Any Stress</li>
 * </ul>
 * 
 * E.g., Search for unstressed consonants</br>
 * <pre>
 * \c:stress("U")
 * </pre>
 * 
 * Stress types may also be 'or'-ed using the pipe ('|') symbol.</br>
 * 
 * E.g., Search for stressed (primary or secondary) consonants</br>
 * <pre>
 * \c:stress("1|2")
 * </pre>
 * </p>
 */
@PhonexPlugin(name = "stress", description="Match stress", arguments= {"type"} )
public class StressPluginProvider implements PluginProvider {

	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args == null)
			throw new NullPointerException();
		if(args.size() != 1) {
			throw new IllegalArgumentException();
		}
		
		final String arg = args.get(0);
		StressMatcher retVal = new StressMatcher();
		
		String[] types = arg.split("\\|");
		for(String typeString:types) {
			SyllableStress stress = SyllableStress.fromString(typeString);
			if(stress != null)
				retVal.addType(stress);
			else 
				throw new IllegalArgumentException("Invalid stress type: " + typeString);
		}
		
		return retVal;
	}

}

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
package ca.phon.syllable.phonex;

import ca.phon.phonex.*;
import ca.phon.syllable.SyllableStress;

import java.util.List;

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

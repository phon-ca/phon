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

import java.util.*;

import ca.phon.phonex.*;
import ca.phon.syllable.*;

/**
 * <p>Phonex plug-in for syllable constituent type matching.  This
 * matcher accepts a list of syllable constituent types separated
 * by the '|' symbol. (e.g., {}:sctype("O|LA") - onset or leftappendix).  Both
 * long and short contituent type identifiers can be used.  For the
 * list of contituent types, see {@link SyllableConstituentType}.</p>
 * 
 * <p>This is the default type matcher in Phonex and can also be written
 * without the plug-in matcher identifier.  E.g., <code>{}:O|LA == {}:sctype("O|LA")</code>.
 * </p>
 */
@PhonexPlugin(name = "sctype", description="Match syllable constituent type", arguments= {"type"} )
public class SyllableConstituentPluginProvider implements PluginProvider {
	
	@Override
	public PhoneMatcher createMatcher(List<String> args)
			throws IllegalArgumentException {
		if(args == null)
			throw new NullPointerException();
		if(args.size() != 1) {
			throw new IllegalArgumentException();
		}
		final String arg = args.get(0);
		SyllableConstituentMatcher retVal = new SyllableConstituentMatcher();
		String[] scTypes = arg.split("\\|");
		for(String scTypeId:scTypes) {
			boolean not = (scTypeId.startsWith("-") ? true : false);
			if(not)
				scTypeId = scTypeId.substring(1);
			SyllableConstituentType scType = SyllableConstituentType.fromString(scTypeId);
			if(scType == null) 
				throw new IllegalArgumentException("Invalid syllable constituent type '" + 
						scTypeId + "'");
			else {
				if(not)
					retVal.getDisallowedTypes().add(scType);
				else
					retVal.getAllowedTypes().add(scType);
			}
		}
		return retVal;
	}

}

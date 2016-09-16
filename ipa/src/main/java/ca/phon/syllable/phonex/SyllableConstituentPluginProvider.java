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
import ca.phon.syllable.SyllableConstituentType;

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
@PhonexPlugin(name = "sctype")
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

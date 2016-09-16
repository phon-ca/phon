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
package ca.phon.ipa.features;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Text collation for IPA transcriptions.  The ordering used is the same
 * ordering found in the <code>features.xml</code> file.
 */
public class IPACollator extends RuleBasedCollator {

	private static String rules = null;
	
	private static final String createRules() {
		if(rules == null) {
			final Set<Character> chars = new LinkedHashSet<>();
			final Set<Character> allChars = FeatureMatrix.getInstance().getCharacterSet();
			for(Character c:allChars) {
				// exclude punctuation
				if(c != 0x28 && c!= 0x29 && c != 0x7c && c != 0x20 && c != 0x2a && c != 0x2e
						&& c != '+' && c != '-') {
					chars.add(c);
				}
			}
			final StringBuffer buffer = new StringBuffer();
			chars.forEach( (c) -> {
					if(buffer.length() > 0) buffer.append(" ");
					buffer.append("< ");
					buffer.append(c);
			});
			rules = buffer.toString();
		}
		return rules;
	}
	
	public IPACollator() throws ParseException {
		super(createRules());
	}
	
}


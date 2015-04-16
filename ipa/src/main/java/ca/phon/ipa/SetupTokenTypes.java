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
package ca.phon.ipa;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

public class SetupTokenTypes {
	
	public static void main(String[] args) throws Exception {
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		final IPATokens ipaTokens = IPATokens.getSharedInstance();
		
		final List<Character> missingChars = new ArrayList<Character>();
		final List<Character> extraChars = new ArrayList<Character>();
		
		for(Character c:fm.getCharacterSet()) {
			final IPATokenType tt = ipaTokens.getTokenType(c);
			if(tt == null) {
				missingChars.add(c);
			}
		}
		
	}

}

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

import java.util.HashSet;
import java.util.Set;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableStress;

/**
 * 
 */
public class StressMatcher implements PhoneMatcher {
	
	/**
	 * List of stress types allowed
	 */
	private Set<SyllableStress> stressTypes = 
			new HashSet<SyllableStress>();
	
	/**
	 * Add the given stress type to the list of
	 * allowed types.
	 * 
	 * @param type
	 */
	public void addType(SyllableStress type) {
		stressTypes.add(type);
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		SyllabificationInfo info = 
				p.getExtension(SyllabificationInfo.class);
		if(info != null) {
			SyllableStress phoneStress = info.getStress();
			retVal = stressTypes.contains(phoneStress)
					|| (stressTypes.contains(SyllableStress.AnyStrress) &&
							(info.getStress() == SyllableStress.PrimaryStress || info.getStress() == SyllableStress.SecondaryStress));
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return (stressTypes.size() == SyllableStress.values().length);
	}

}

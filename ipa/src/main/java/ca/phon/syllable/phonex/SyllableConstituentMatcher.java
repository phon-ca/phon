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

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Syllable constituent type matcher for phonex.
 */
public final class SyllableConstituentMatcher implements PhoneMatcher {
	
	/**
	 * Allowed constituent types
	 */
	private final List<SyllableConstituentType> allowedTypes = 
			new ArrayList<SyllableConstituentType>();

	/**
	 * Dis-allowed constituent types
	 */
	private final List<SyllableConstituentType> disallowedTypes = 
			new ArrayList<SyllableConstituentType>();
	
	/**
	 * Constructor
	 */
	public SyllableConstituentMatcher() {
		
	}
	
	/**
	 * Access to the allowed types list
	 */
	public List<SyllableConstituentType> getAllowedTypes() {
		return this.allowedTypes;
	}
	
	/**
	 * Access to the disallowed types list
	 */
	public List<SyllableConstituentType> getDisallowedTypes() {
		return this.disallowedTypes;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		if(matchesAnything()) return true;
		
		boolean retVal = true;
		SyllabificationInfo scInfo = p.getExtension(SyllabificationInfo.class);
		if(scInfo != null) {
			if(allowedTypes.size() > 0) {
				if(scInfo.getConstituentType() == SyllableConstituentType.AMBISYLLABIC) {
					retVal &= (allowedTypes.contains(SyllableConstituentType.AMBISYLLABIC)
							|| allowedTypes.contains(SyllableConstituentType.ONSET)
							|| allowedTypes.contains(SyllableConstituentType.CODA));
				} else 
					retVal &= allowedTypes.contains(scInfo.getConstituentType());
			}
			if(disallowedTypes.size() > 0)
				retVal &= !disallowedTypes.contains(scInfo.getConstituentType());
		} else {
			retVal = false;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return (allowedTypes.size() == 0 && disallowedTypes.size() == 0);
	}

	@Override
	public String toString() {
		String retVal = "";
		
		for(SyllableConstituentType scType:allowedTypes) {
			retVal += (retVal.length() > 0 ? "|":"") + scType.getIdentifier();
		}
		for(SyllableConstituentType scType:disallowedTypes) {
			retVal += (retVal.length() > 0 ? "|":"") + "-" + scType.getIdentifier();
		}
		
		return retVal;
	}
}

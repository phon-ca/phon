/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.phonex.plugins.CombinableMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Syllable constituent type matcher for phonex.
 */
public final class SyllableConstituentMatcher implements PhoneMatcher, CombinableMatcher {
	
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

	@Override
	public void combineMatcher(PhoneMatcher matcher) {
		if(!(matcher instanceof SyllableConstituentMatcher))
			throw new IllegalArgumentException();
		
		final SyllableConstituentMatcher scMatcher = (SyllableConstituentMatcher)matcher;
		
		allowedTypes.addAll(scMatcher.allowedTypes);
		disallowedTypes.addAll(scMatcher.disallowedTypes);
	}
	
}

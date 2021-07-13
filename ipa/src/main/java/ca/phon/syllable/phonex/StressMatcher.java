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

import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.syllable.*;

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
					|| (stressTypes.contains(SyllableStress.AnyStress) &&
							(info.getStress() == SyllableStress.PrimaryStress || info.getStress() == SyllableStress.SecondaryStress));
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return stressTypes.contains(SyllableStress.AnyStress) ||
				(stressTypes.contains(SyllableStress.NoStress) && stressTypes.contains(SyllableStress.PrimaryStress) && stressTypes.contains(SyllableStress.SecondaryStress));
	}

	@Override
	public String toString() {
		StringBuilder retVal = new StringBuilder();

		this.stressTypes.forEach( type -> {
			if(!retVal.isEmpty())
				retVal.append("|");
			switch(type) {
				case PrimaryStress:
					retVal.append("1");
					break;

				case SecondaryStress:
					retVal.append("2");
					break;

				case NoStress:
					retVal.append("U");
					break;

				case AnyStress:
					retVal.append("S");
					break;

				default:
					break;
			}
		});

		return retVal.toString();
	}

}

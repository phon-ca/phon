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

package ca.phon.syllable;

import java.awt.*;

/**
 * Parts of a syllable. Every phone may have one of these
 * types associated with it.
 */
public enum SyllableConstituentType {
	LEFTAPPENDIX("LA", "L"),
	ONSET("O", "O"),
	NUCLEUS("N", "N"),
	CODA("C", "C"),
	RIGHTAPPENDIX("RA", "R"),
	OEHS("OEHS", "E"),
	AMBISYLLABIC("AS", "A"),
	UNKNOWN("UK", "U"),
	SYLLABLEBOUNDARYMARKER("SB", "B"),
	SYLLABLESTRESSMARKER("SS", "S"),
	WORDBOUNDARYMARKER("WB", "W");
	
	private String shortHand;
	
	private String mnemonic;
	
	private SyllableConstituentType(String sh, String mnemonic) {
		this.shortHand = sh;
		this.mnemonic = mnemonic;
	}
	
	public String getIdentifier() {
		return shortHand;
	}
	
	public char getIdChar() {
		return mnemonic.charAt(0);
	}
	
	public String getMnemonic() {
		return this.mnemonic;
	}
	
	/**
	 * Return the constituent type for the given identifier.
	 * 
	 * @param identifier
	 * @return the constituent type or {@link SyllableConstituentType#UNKNOWN} if
	 *  not found
	 */
	public static SyllableConstituentType fromString(String identifier) {
		SyllableConstituentType retVal = null;
		
		// special case for diphthongs
		if(identifier.equalsIgnoreCase("D")) {
			retVal = NUCLEUS;
		} else {
			for(SyllableConstituentType v:values()) {
				if(v.toString().equalsIgnoreCase(identifier) || v.getIdentifier().equalsIgnoreCase(identifier)
						|| v.mnemonic.equalsIgnoreCase(identifier)) {
					retVal = v;
					break;
				}
			}
		}
		
		return retVal;
	}
}

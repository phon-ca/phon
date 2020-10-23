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

import ca.phon.util.*;

/**
 * Parts of a syllable. Every phone may have one of these
 * types associated with it.
 */
public enum SyllableConstituentType {
	LEFTAPPENDIX("LA", "L", PrefHelper.getColor("ui.leftappendix.color", Color.decode("0xffe13c"))),
	ONSET("O", "O", PrefHelper.getColor("ui.onset.color", Color.decode("0x307ECC"))),
	NUCLEUS("N", "N", PrefHelper.getColor("ui.nucleus.color", Color.decode("0xFE3C3C"))),
	CODA("C", "C", PrefHelper.getColor("ui.coda.color", Color.decode("0x5BA151"))),
	RIGHTAPPENDIX("RA", "R", PrefHelper.getColor("ui.rightappendix.color", Color.decode("0xFF8A3C"))),
	OEHS("OEHS", "E", PrefHelper.getColor("ui.oehs.color", Color.decode("0x3cd3c3"))),
	AMBISYLLABIC("AS", "A", PrefHelper.getColor("ui.ambisyllabic.color", Color.decode("0x6C9BA1"))),
	UNKNOWN("UK", "U", PrefHelper.getColor("ui.unknown.color", Color.white)),
	SYLLABLEBOUNDARYMARKER("SB", "B", PrefHelper.getColor("ui.syllableboundary.color", Color.white)),
	SYLLABLESTRESSMARKER("SS", "S", PrefHelper.getColor("ui.stress.color", Color.lightGray)),
	WORDBOUNDARYMARKER("WB", "W", PrefHelper.getColor("ui.wordboundary.color", Color.white));
	
	private String shortHand;
	
	private String mnemonic;
	
	private Color uiColor;
	
	private SyllableConstituentType(String sh, String mnemonic, Color c) {
		this.shortHand = sh;
		this.mnemonic = mnemonic;
		this.uiColor = c;
	}
	
	public String getIdentifier() {
		return shortHand;
	}
	
	public char getIdChar() {
		return mnemonic.charAt(0);
	}
	
	public Color getColor() {
		return uiColor;
	}
	
	public String getMnemonic() {
		return this.mnemonic;
	}
	
	/**
	 * Return the constituent type for the given identifier.
	 * 
	 * @param identifier
	 * @return the constituent type or {@link UKNOWN} if
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

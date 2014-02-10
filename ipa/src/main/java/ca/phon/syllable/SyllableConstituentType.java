/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.syllable;

import java.awt.Color;

import ca.phon.util.PrefHelper;

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
		SyllableConstituentType retVal = SyllableConstituentType.UNKNOWN;
		
		for(SyllableConstituentType v:values()) {
			if(v.toString().equalsIgnoreCase(identifier) || v.getIdentifier().equalsIgnoreCase(identifier)
					|| v.mnemonic.equals(identifier)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

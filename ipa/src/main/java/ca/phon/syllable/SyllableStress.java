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

package ca.phon.syllable;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPATranscript;

/**
 * Represents syllable stress.
 * @author ghedlund
 *
 */
@Extension(IPATranscript.class)
public enum SyllableStress {
	PrimaryStress("1", '\u02c8'),
	SecondaryStress("2", '\u02cc'),
	AnyStrress("S", (char)0),
	NoStress("U", (char)0);
	
	private String idString;
	
	private char ipa;
	
	private SyllableStress(String s, char ipa) {
		idString = s;
		this.ipa = ipa;
	}
	
	public String getId() {
		return this.idString;
	}
	
	public char getIpa() {
		return this.ipa;
	}
	
	public static SyllableStress fromString(String text) {
		SyllableStress retVal = null;
		
		for(SyllableStress type:values()) {
			if(type.toString().equalsIgnoreCase(text) || type.getId().equalsIgnoreCase(text)) {
				retVal = type;
				break;
			}
		}
		
		return retVal;
	}
}

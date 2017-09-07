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
package ca.phon.cvseq;

import java.util.*;

/**
 *
 */
public enum CVSeqType {
	
	Consonant,
	Glide,
	Vowel,
	DontCare,
	DontCareExceptIfWordBoundary,
	WordBoundary;
	
	public static final char[] images = {
		'C',
		'G',
		'V',
		'B',
		'A',
		' '
	};
	
	public static char getImage(CVSeqType type) {
		return type.getImage();
	}
	
	public char getImage() {
		return images[ordinal()];
	}
	
	public static CVSeqType matcherFromImage(char img, boolean ignoreCase) {
		char testImg = 
			(ignoreCase ? Character.toUpperCase(img) : img);
		
		CVSeqType retVal = null;
		for(CVSeqType t:CVSeqType.values()) {
			if(t.getImage() == testImg) {
				retVal = t;
				break;
			}
		}
		return retVal;
	}
	
	public boolean matches(CVSeqType m) {
		boolean retVal = false;
		
		if(this == DontCare) {
			retVal = true;
		} else if(this == DontCareExceptIfWordBoundary) {
			if(m != WordBoundary)
				retVal = true;
		} else if(this == Consonant) {
			if(m == Consonant || m == Glide)
				retVal = true;
		} else {
			if(m == this)
				retVal = true;
		}
		
		return retVal;
	}
	
	public static List<CVSeqType> toCVSeqMatcherList(String txt) {
		List<CVSeqType> retVal = new ArrayList<CVSeqType>(txt.length());
		
		for(char c:txt.toCharArray()) {
			CVSeqType t = CVSeqType.matcherFromImage(c, true);
			if(t == null) {
				throw new IllegalArgumentException("Invalid phone type: '" + c + "'");
			}
			retVal.add(t);
		}
		
		return retVal;
	}
	
}

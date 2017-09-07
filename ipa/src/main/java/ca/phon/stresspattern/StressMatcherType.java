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
package ca.phon.stresspattern;

import java.util.*;

public enum StressMatcherType {
	
	Unstressed,
	PrimaryStressed,
	SecondaryStressed,
	Stressed,
	DontCare,
	DontCareExceptIfWordBoundary,
	WordBoundary;
	
	public static final char[] images = {
			'U',
			'1',
			'2',
			'S',
			'B',
			'A',
			' '
	};
	
	public static char getImage(StressMatcherType st) {
		return st.getImage();
	}
	
	public char getImage() {
		return images[ordinal()];
	}

	public static StressMatcherType matcherFromImage(char img, boolean ignoreCase) {
		char testImg = 
			(ignoreCase ? Character.toUpperCase(img) : img);
		
		StressMatcherType retVal = null;
		for(StressMatcherType st:values()) {
			if(st.getImage() == testImg) {
				retVal = st;
				break;
			}
		}
		return retVal;
	}
	
	public boolean matches(StressMatcherType stType) {
		boolean retVal = false;
		// check non-specific matchers first
		if(this == DontCare)
			retVal = true;
		else if(this == DontCareExceptIfWordBoundary) {
			if(stType != WordBoundary)
				retVal = true;
		} else if(this == Stressed) {
			if(stType == PrimaryStressed || 
					stType == SecondaryStressed ||
					stType == Stressed)
				retVal = true;
		} else {
			retVal = (this == stType);
		}
		
		return retVal;
	}
	
	public static List<StressMatcherType> toStressMatcherList(String stressPattern)
		throws IllegalArgumentException {
		ArrayList<StressMatcherType> retVal = 
			new ArrayList<StressMatcherType>(stressPattern.length());
		
		for(char c:stressPattern.toCharArray()) {
			StressMatcherType fromImage = matcherFromImage(c, true);
			if(fromImage == null) {
				throw new IllegalArgumentException("Invalid stress type: '" + c + "'");
			}
			retVal.add(fromImage);
		}
		
		return retVal;
	}
	
}

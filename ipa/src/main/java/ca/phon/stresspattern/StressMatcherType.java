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

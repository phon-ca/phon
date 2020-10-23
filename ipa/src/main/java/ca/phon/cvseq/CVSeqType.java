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

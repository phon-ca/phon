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

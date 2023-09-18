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

package ca.phon.orthography;

import ca.phon.util.Documentation;

/**
 * Codes for word prefix.
 */
public enum WordType {
	@Documentation("https://talkbank.org/manuals/CHAT.html#OmittedWord_Code")
	OMISSION("0", "omission"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Nonword")
	NONWORD("&~", "nonword"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Filler_Code")
	FILLER("&-", "filler"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#PhonologicalFragment_Code")
	FRAGMENT("&+", "fragment");
	
	private String code;
	
	private String displayName;
	
	private WordType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}

	public static WordType fromString(String text) {
		WordType retVal = null;
		
		for(WordType v:values()) {
			if(v.getCode().equals(text) || v.getDisplayName().equals(text)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

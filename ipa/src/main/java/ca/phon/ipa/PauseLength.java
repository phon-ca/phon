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
package ca.phon.ipa;

/**
 * Pause lengths
 */
public enum PauseLength {
	SHORT("."),
	MEDIUM(".."),
	LONG("...");
	
	private String image;
	
	private PauseLength(String image) {
		this.image = image;
	}
	
	/**
	 * Length from string
	 * 
	 * @param text valid values must match the regex
	 *  <code>'\.{1,3}'</code>
	 * @return the detected PauseLength
	 * @throws IllegalArgumentException if the given text
	 *  is not a valid length string
	 */
	public static PauseLength lengthFromString(String text) {
		PauseLength retVal = null;
		if(!text.matches("\\.{1,3}")) {
			throw new IllegalArgumentException("Invalid length string '" + text + "'");
		}
		int len = text.length();
		switch(len) {
		case 1:
			retVal = SHORT;
			break;
			
		case 2:
			retVal = MEDIUM;
			break;
			
		case 3:
			retVal = LONG;
			break;
			
		default:
			throw new IllegalArgumentException("Invalid length string '" + text + "'");	
		}
		return retVal;
	}
	
	/**
	 * Get the length string for
	 * 
	 * @return the text for this length
	 */
	public String getText() {
		return this.image;
	}
}

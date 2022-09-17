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
package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents a pause in an IPA transcription.
 * 
 */
public final class Pause extends IPAElement {
	
	/**
	 * Pause length
	 */
	private PauseLength length;
	
	/**
	 * Constructor
	 * 
	 * @param len the lengths as a string
	 */
	Pause(String len) {
		this(PauseLength.lengthFromString(len));
		
		setScType(SyllableConstituentType.SYLLABLEBOUNDARYMARKER);
	}
	
	/**
	 * Constructor
	 * 
	 * @param len the pause length
	 */
	Pause(PauseLength len) {
		this.length = len;
	}
	
	/**
	 * Get the length of the pause
	 * 
	 * @return the pause length
	 */
	public PauseLength getLength() {
		return this.length;
	}
	
	/**
	 * Set the pause length
	 * 
	 * @param len
	 */
	public void setLength(PauseLength len) {
		this.length = len;
	}
	
	/**
	 * Set the pause length as a string.
	 * 
	 * @param len the text
	 * @throws IllegalArgumentException if the given
	 *  string is not a valid length string
	 */
	public void setLength(String len) {
		setLength(PauseLength.lengthFromString(len));
	}

	
	@Override
	protected FeatureSet _getFeatureSet() {
		// TODO: Setup a proper feature set for pauses
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return "(" + length.getText() + ")";
	}

}

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

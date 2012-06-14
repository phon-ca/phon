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

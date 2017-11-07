package ca.phon.syllable;

import ca.phon.ipa.*;

/**
 * Extension which is added an {@link IPATranscript} object
 * if the syllable is prefixed by an {@link IntraWordPause} character.
 *
 */
public class Segregated {

	private final boolean segregated;
	
	public Segregated(boolean segregated) {
		this.segregated = segregated;
	}
	
	public boolean isSegregated() {
		return this.segregated;
	}

}

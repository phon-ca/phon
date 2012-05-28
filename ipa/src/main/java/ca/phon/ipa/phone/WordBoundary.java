package ca.phon.ipa.phone;

import ca.phon.ipa.featureset.FeatureSet;

/**
 * Represents a boundary between words (i.e., a 'space'.)
 * 
 */
public final class WordBoundary extends Phone {

	@Override
	protected FeatureSet _getFeatureSet() {
		// TODO: Fix feature set for WordBoundary
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return " ";
	}
	
}

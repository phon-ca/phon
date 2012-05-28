package ca.phon.ipa.phone;

import ca.phon.ipa.featureset.FeatureSet;

/**
 * Represents a syllable boundary between words.
 * While other types of {@link Phone}s can also
 * represent a syllable boundary, this is specifically
 * for the 'forced' syllable boundary marked by a '.'
 * in the transcript.
 * 
 */
public final class SyllableBoundary extends Phone {

	@Override
	protected FeatureSet _getFeatureSet() {
		// TODO Fix feature set for syllable boundary
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return ".";
	}

}

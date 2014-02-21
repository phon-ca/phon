package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * Compound word marker.
 *
 */
public class CompoundWordMarker extends IPAElement {

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return "+";
	}

}

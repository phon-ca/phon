package ca.phon.ipa;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

/**
 * Sandhi including contraction and linkers.
 */
public abstract class Sandhi extends IPAElement {

	@Override
	protected FeatureSet _getFeatureSet() {
		final String text = getText();
		final FeatureMatrix fm = FeatureMatrix.getInstance();
		return fm.getFeatureSet(text.charAt(0));
	}

}

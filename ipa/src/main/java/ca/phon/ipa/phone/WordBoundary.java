package ca.phon.ipa.phone;

import ca.phon.ipa.featureset.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents a boundary between words (i.e., a 'space'.)
 * 
 */
public final class WordBoundary extends Phone {
	
	public WordBoundary() {
		super();
		
		setScType(SyllableConstituentType.WORDBOUNDARYMARKER);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return " ";
	}
	
}

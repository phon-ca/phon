package ca.phon.ipa.elements;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents a boundary between words (i.e., a 'space'.)
 * 
 */
public final class WordBoundary extends IPAElement {
	
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

package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

public class IntraWordPause extends IPAElement {
	
	public static final Character INTRA_WORD_PAUSE_CHAR = '^';
	
	public IntraWordPause() {
		setScType(SyllableConstituentType.SYLLABLEBOUNDARYMARKER);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return new StringBuilder().append(INTRA_WORD_PAUSE_CHAR).toString();
	}

}

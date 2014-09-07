package ca.phon.ipa;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Character used to indicate alignment.
 */
public class AlignmentMarker extends IPAElement {
	
	public final static char ALIGNMENT_CHAR = '\u2194';
	
	public AlignmentMarker() {
		super();
		setScType(SyllableConstituentType.UNKNOWN);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return FeatureMatrix.getInstance().getFeatureSet(ALIGNMENT_CHAR);
	}

	@Override
	public String getText() {
		return "" + ALIGNMENT_CHAR;
	}

}

package ca.phon.ipa.phone;

import ca.phon.ipa.featureset.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents Major/Minor intonation group markers.
 * 
 */
public class IntonationGroup extends Phone {
	
	/**
	 * group type
	 */
	private IntonationGroupType type;
	
	/**
	 * Constructor
	 * 
	 * @param type
	 */
	public IntonationGroup(IntonationGroupType type) {
		this.type = type;
		
		setScType(SyllableConstituentType.SYLLABLEBOUNDARYMARKER);
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return type.getGlyph() + "";
	}
	
}

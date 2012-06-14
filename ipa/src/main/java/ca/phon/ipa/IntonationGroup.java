package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Represents Major/Minor intonation group markers.
 * 
 */
public class IntonationGroup extends IPAElement {
	
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

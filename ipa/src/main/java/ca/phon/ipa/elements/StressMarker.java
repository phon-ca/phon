package ca.phon.ipa.elements;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;

/**
 * A stress marker.  Stress markers can either be
 * PRIMARY or SECONDARY.
 */
public final class StressMarker extends IPAElement {
	
	/**
	 * Stress type
	 */
	private StressType type;
	
	/**
	 * Constructor
	 * 
	 * @param stress
	 */
	StressMarker(StressType stress) {
		this.type = stress;
		
		setScType(SyllableConstituentType.SYLLABLESTRESSMARKER);
	}

	/**
	 * Get the type
	 * 
	 * @return StressType
	 */
	public StressType getType() {
		return this.type;
	}
	
	/**
	 * Set the type
	 * 
	 * @param type
	 */
	public void setType(StressType type) {
		this.type = type;
	}
	
	@Override
	protected FeatureSet _getFeatureSet() {
		return FeatureMatrix.getInstance().getFeatureSet(type.getGlyph());
	}

	@Override
	public String getText() {
		return type.getGlyph() + "";
	}

}

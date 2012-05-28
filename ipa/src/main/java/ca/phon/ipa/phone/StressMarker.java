package ca.phon.ipa.phone;

import ca.phon.ipa.featureset.FeatureMatrix;
import ca.phon.ipa.featureset.FeatureSet;

/**
 * A stress marker.  Stress markers can either be
 * PRIMARY or SECONDARY.
 */
public final class StressMarker extends Phone {
	
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

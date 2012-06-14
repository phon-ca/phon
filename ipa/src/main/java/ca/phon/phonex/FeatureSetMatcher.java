package ca.phon.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

/**
 * Class to match phones based on 
 * feature sets.
 * 
 */
public final class FeatureSetMatcher implements PhoneMatcher {
	
	/**
	 * Required features
	 * 
	 */
	private final FeatureSet requiredFeatures = new FeatureSet();
	
	/**
	 * Not features
	 */
	private final FeatureSet notFeatures = new FeatureSet();
	
	/**
	 * Constructor
	 * 
	 */
	public FeatureSetMatcher() {
		this(new FeatureSet(), new FeatureSet());
	}
	
	public FeatureSetMatcher(FeatureSet requiredFeatures) {
		this(requiredFeatures, new FeatureSet());
	}
	
	public FeatureSetMatcher(FeatureSet requiredFeatures, FeatureSet notFeatures) {
		this.requiredFeatures.union(requiredFeatures);
		this.notFeatures.union(notFeatures);
	}
	
	/**
	 * Add a new required feature to the set
	 * 
	 * @param featureName
	 * @throws IllegalArgumentException if the given feature
	 *  name is not a valid feature
	 */
	public void addRequiredFeature(String featureName) {
		FeatureSet fs = FeatureMatrix.getInstance().getFeatureSetForFeature(featureName);
		if(fs == null)
			throw new IllegalArgumentException("'" + featureName + "' is not a valid feature name.");
		this.requiredFeatures.union(fs);
	}
	
	/**
	 * Add a feature that is required NOT to be in 
	 * the given phone's feature set.
	 * 
	 * @param featureName
	 * @throws IllegalArgumentException if the given feature
	 *  name is not a vaild feature.
	 */
	public void addNotFeature(String featureName) {
		FeatureSet fs = FeatureMatrix.getInstance().getFeatureSetForFeature(featureName);
		if(fs == null)
			throw new IllegalArgumentException("'" + featureName + "' is not a valid feature name.");
		this.notFeatures.union(fs);
	}

	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		FeatureSet phoneFs = p.getFeatureSet();
		
		FeatureSet reqFs = FeatureSet.intersect(phoneFs, requiredFeatures);
		FeatureSet notFs = FeatureSet.intersect(phoneFs, notFeatures);
		
		retVal =
				((reqFs.equals(requiredFeatures)) && (notFs.size() == 0));
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		boolean retVal = 
				requiredFeatures.size() == 0
				&& notFeatures.size() == 0;
		return retVal;
	}
	

	@Override
	public String toString() {
		String retVal = "{";
		for(String featureName:requiredFeatures.getFeatures()) {
			retVal += (retVal.length() > 1 ? "," : "") + featureName;
		}
		for(String featureName:notFeatures.getFeatures()) {
			retVal += (retVal.length() > 1 ? "," : "") + "-" + featureName;
		}
		retVal += "}";
		
		return retVal;
	}
}

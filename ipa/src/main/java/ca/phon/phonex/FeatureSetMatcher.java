/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.phonex;

import ca.phon.ipa.*;
import ca.phon.ipa.features.*;

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
	private FeatureSet requiredFeatures = new FeatureSet();
	
	/**
	 * Not features
	 */
	private FeatureSet notFeatures = new FeatureSet();
	
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
		this.requiredFeatures = FeatureSet.union(this.requiredFeatures, requiredFeatures);
		this.notFeatures = FeatureSet.union(this.notFeatures, notFeatures);
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
		this.requiredFeatures = FeatureSet.union(this.requiredFeatures, fs);
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
		this.notFeatures = FeatureSet.union(this.notFeatures, fs);
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

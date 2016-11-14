/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

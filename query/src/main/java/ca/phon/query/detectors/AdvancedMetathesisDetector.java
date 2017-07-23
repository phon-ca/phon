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
package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public class AdvancedMetathesisDetector extends BasicMetathesisDetector {

	private FeatureSet features;
	
	private FeatureSet absentFeatures;
	
	public AdvancedMetathesisDetector() {
		super();
		features = null;
	}
	
	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> potentialResults = super.detect(pm);
		final List<DetectorResult> retVal = new ArrayList<DetectorResult>();
		
		// filter results
		for(DetectorResult potentialResult:potentialResults) {
			final MetathesisDetectorResult r = (MetathesisDetectorResult)potentialResult;
			boolean addResult = true;
			
			if(features != null && features.size() > 0) {
				addResult &= r.getFeatures1().intersects(features) 
					|| r.getFeatures2().intersects(features);
			}
			
			if(absentFeatures != null && absentFeatures.size() > 0) {
				addResult &= !r.getFeatures2().intersects(absentFeatures) 
						&& !r.getFeatures1().intersects(absentFeatures);
			}
			
			if(addResult)
				retVal.add(r);
		}
		
		return retVal;
	}

	public FeatureSet getFeatures() {
		return (features == null ? new FeatureSet() : features);
	}


	public void setFeatures(FeatureSet containedFeatures) {
		this.features = containedFeatures;
	}

	public FeatureSet getAbsentFeatures() {
		return absentFeatures;
	}

	public void setAbsentFeatures(FeatureSet absentFeatures) {
		this.absentFeatures = absentFeatures;
	}
	
}

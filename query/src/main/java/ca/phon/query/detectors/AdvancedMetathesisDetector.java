/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;

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

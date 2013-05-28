/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.phon.application.transcript.IWord;
import ca.phon.engines.detectors.DetectorResult;
import ca.phon.engines.detectors.MetathesisDetector;
import ca.phon.featureset.FeatureSet;

public class SMetathesisDetector implements SDetector {

	/** Detector */
	private MetathesisDetector detector;
	
	private FeatureSet features;
	
	private FeatureSet absentFeatures;
	
	public SMetathesisDetector() {
		this.detector = new MetathesisDetector();
		features = null;
	}

	@Override
	public SDetectorResult[] detect(SRecord record, int group) {
		List<SDetectorResult> retVal = new ArrayList<SDetectorResult>();
		
		if(group < 0 || group >= record.getNumberOfGroups()) {
			return new SDetectorResult[0];
		}
		
		IWord grp = record._getUtt().getWords().get(group);
		Collection<DetectorResult> detectedResults = detector.detect(grp);
		
		// filter results
		for(DetectorResult r:detectedResults) {
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
				retVal.add(new SDetectorResult(record._getUtt(), record._getUttIndex(), group, r));
		}
		
		return retVal.toArray(new SDetectorResult[0]);
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

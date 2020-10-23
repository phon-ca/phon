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
package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.ipa.features.*;

/**
 * A class that can detect metatheses in a word based on an
 * actual form. Multiple interpertations can exist.
 *
 */
public class BasicMetathesisDetector extends Detector
{    
	/*
	 * Detector implementation
	 */
	@Override
	public void performDetection() {
		detect_around_vowel();
		detect_at_ends();
	}

	/**
	 * Detect metathesis that happens when a consonant is swapped to the end
	 * and store the results.
	 */
	private void detect_at_ends() {
		int end = map.getAlignmentLength() - 1;
		if(end <= 0) return; // one or no aligned pairs

		// Check to see whether target/actual is an indel at the start of
		// the alignment
		List<IPAElement> pair1 = null;
		List<IPAElement> pair2 = null;

		int i = 0;
		for(; i < map.getAlignmentLength(); ++i) {
			pair1 = map.getAlignedElements(i);
			if(pair1.get(0) != null && !pair1.get(0).getFeatureSet().hasFeature("Consonant")) continue;
			if(pair1.get(1) != null && !pair1.get(1).getFeatureSet().hasFeature("Consonant")) continue;
			if(pair1.get(0) != null && pair1.get(1) != null)
				return;
			
			break;
		}

		int j = end;
		for(; j > i; --j) {
			pair2 = map.getAlignedElements(j);
			if(pair2.get(0) != null && !pair2.get(0).getFeatureSet().hasFeature("Consonant")) continue;
			if(pair2.get(1) != null && !pair2.get(1).getFeatureSet().hasFeature("Consonant")) continue;
			if(pair2.get(0) != null && pair2.get(1) != null)
				return;
			
			// Make sure that the consonant is in the correct position
			if((pair1.get(0) == null || pair2.get(1) == null) && (pair1.get(1) == null && pair2.get(0) == null))
				return;

			break;
		}

		// No pairs to work with
		if(pair1 == null || pair2 == null || j <= i) return;

		// Based on which is an indel at the start, the opposite form should
		// not have an indel at the end, so check to make sure of this and
		// get the feature sets for both
		FeatureSet fs1 = new FeatureSet();
		if(pair1.get(0) != null && pair2.get(1) != null)
			fs1 = FeatureSet.intersect(pair1.get(0).getFeatureSet(), pair2.get(1).getFeatureSet());
		
		FeatureSet fs2 = new FeatureSet();
		if(pair1.get(1) != null && pair2.get(0) != null)
			fs2 = FeatureSet.intersect(pair1.get(1).getFeatureSet(), pair2.get(0).getFeatureSet());
		
		final MetathesisDetectorResult r = new MetathesisDetectorResult(map);
		r.setFirstPosition(i);
		r.setSecondPosition(j);
		r.setFeatures1(fs1);
		r.setFeatures2(fs2);
		addResult(r);
	}

	/**
	 * Detects any metathesis that occurs on consonants adjacent to
	 * a vowel and stores the results.
	 */
	private void detect_around_vowel() {
		int len = map.getAlignmentLength();

		// Go through the adjacent pairs
		for(int i = 0; i < len; ++i) {
			// Get aligned pairs
			List<IPAElement> pair1 = map.getAlignedElements(i);
			List<IPAElement> pair2 = null;
//			if(pair1.get(0) == null || pair1.get(1) == null)
//				continue;
			
			final IPAElement ele1 = pair1.get(0);
			final IPAElement ele2 = pair1.get(1);

			// Get feature sets for previous consonant
			FeatureSet fsTargetL = (ele1 != null ? pair1.get(0).getFeatureSet() : new FeatureSet());
			FeatureSet fsActualL = (ele2 != null ? pair1.get(1).getFeatureSet() : new FeatureSet());
			
			boolean bothC = fsTargetL.hasFeature("Consonant") && fsActualL.hasFeature("Consonant");
			boolean targetC = fsActualL.size() == 0 && fsTargetL.hasFeature("Consonant");
			boolean actualC = fsTargetL.size() == 0 && fsActualL.hasFeature("Consonant");
			if(!bothC && !targetC && !actualC) continue;

			// Get feature sets for the following consonant pair
			FeatureSet fsTargetR = null, fsActualR = null;
			int j = i+1;
			boolean vowelFound = false;
			for(; j < len; ++j) {
				pair2 = map.getAlignedElements(j);
//				if(pair2.get(0) == null || pair2.get(1) == null)
//					continue;

				fsTargetR = (pair2.get(0) != null ? pair2.get(0).getFeatureSet() : new FeatureSet());
				fsActualR = (pair2.get(1) != null ? pair2.get(1).getFeatureSet() : new FeatureSet());
				
				bothC = fsTargetR.hasFeature("Consonant") && fsActualR.hasFeature("Consonant");
				targetC = fsTargetR.hasFeature("Consonant") && fsActualR.size() == 0;
				actualC = fsTargetR.size() == 0 && fsActualR.hasFeature("Consonant");
				
				if(bothC || targetC || actualC) {
					if(vowelFound) {
						break;
					}
				} else {
					vowelFound = true;
				}
			}

			// Didn't find a proper consonant, so just exit
			if(j >= len) break;

			// Intersect pairs and remove anything common to both
			FeatureSet f1 = FeatureSet.intersect(fsTargetL, fsActualR);
			FeatureSet f2 = FeatureSet.intersect(fsTargetR, fsActualL);

			FeatureSet common = FeatureSet.intersect(f1, f2);
			f1 = FeatureSet.minus(f1, common);
			f2 = FeatureSet.minus(f2, common);

			// Remove anything that is not exclusive to f1/f2
			f1 = FeatureSet.minus(f1, FeatureSet.union(fsActualL, fsTargetR));
			f2 = FeatureSet.minus(f2, FeatureSet.union(fsActualR, fsTargetL));

			// If there are still features in both then we're in business
			if( (f1.size() > 0 && f2.getFeatures().size() > 0)
					|| (fsActualL.size() == 0 && f1.size() > 0)
					|| (fsActualR.size() == 0 && f2.size() > 0)) {
				MetathesisDetectorResult r = new MetathesisDetectorResult(map);
				r.setFirstPosition(i);
				r.setSecondPosition(j);
				r.setFeatures1(f1);
				r.setFeatures2(f2);
				addResult(r);
			}
		}
	}

	/**
	 * Adds the result to the list of results. Takes care of merging the
	 * result with another.
	 * @param r  the Result to add
	 */
	private void addResult(MetathesisDetectorResult r) {
		// Remove any unwanted/obvious features and ensure that the result
		// then has *some* features swapped
		r.setFeatures1(FeatureSet.minus(r.getFeatures1(), FeatureSet.fromArray(new String[]{ "Consonant, Diacritic" })));
		r.setFeatures2(FeatureSet.minus(r.getFeatures2(), FeatureSet.fromArray(new String[]{ "Consonant, Diacritic" })));
		
		//if(r.getFeatures1().getFeatures().size() == 0
		//		|| r.getFeatures2().getFeatures().size() == 0)
		if(r.getFeatures1().size() == 0
				&& r.getFeatures2().size() == 0)
			return;

		// Merge results if existing metathesis at the same
		// position, otherwise create a new result
		for(DetectorResult result : results) {
			final MetathesisDetectorResult res = (MetathesisDetectorResult)result;
			if(res.getFirstPosition() == r.getFirstPosition() &&
					res.getSecondPosition() == r.getSecondPosition())
			{					
				res.setFeatures1( FeatureSet.union(res.getFeatures1(), r.getFeatures1()) );
				res.setFeatures2( FeatureSet.union(res.getFeatures2(), r.getFeatures2()) );
				return;
			}
		}
		results.add(r);
	}

}

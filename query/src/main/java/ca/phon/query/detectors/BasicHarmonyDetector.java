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

import ca.phon.ipa.*;
import ca.phon.ipa.features.*;

/**
 * A class that can detect consonant/vowel harmony in a word based on
 * an actual form. Multiple interpertations can exist.
 */
public class BasicHarmonyDetector extends Detector {
	// true for consonant harmony, false for vowel harmony
	private boolean consonants;

	/**
	 * Default constructor.
	 */
    public BasicHarmonyDetector(boolean consonants) {
    	super();
    	this.consonants = consonants;
    }
    
    public boolean isConsonants() {
    	return this.consonants;
    }
    
    public void setConsonants(boolean consonants) {
    	this.consonants = consonants;
    }
    
    /*
     * Detector implementation.
     */
    @Override
	public void performDetection() {
    	detect_harmony(true);  // left to right
    	detect_harmony(false); // right to left
    }

    /**
     * Detects metathesis and stores the results.
     * @param target  ArrayList of Phones for target form
	 * @param actual  ArrayList of Phones for actual form
	 * @param 
     */
    private void detect_harmony(boolean leftToRight) {    	
    	// A list to keep track of current harmonies
    	ArrayList<HarmonyDetectorResult> current = new ArrayList<>();
    	
    	// The Consonant/Vowel feature depending on the type of detection
    	// specified by the `consonants` variable
    	String lookFor = (consonants ? "Consonant" : "Vowel");
    	
    	// Perform for each feature
    	int len = map.getAlignmentLength();
   		for(int i = 0; i < len; ++i) {
   			// Get aligned pairs
   			int index = (leftToRight ? i : len - i - 1);
   			List<IPAElement> pair1 = map.getAlignedElements(index);
   			
   			IPAElement ele1 = pair1.get(0);
   			IPAElement ele2 = pair1.get(1);
   			
   			// Get feature sets
   			FeatureSet fsTarget = (ele1 != null ? ele1.getFeatureSet() : new FeatureSet());
   			FeatureSet fsActual = (ele2 != null ? ele2.getFeatureSet() : new FeatureSet());
   			if((fsTarget.size() > 0 && !fsTarget.hasFeature(lookFor))
   					|| (fsActual.size() > 0 && !fsActual.hasFeature(lookFor)) )
   				continue;
   			
   			// Find out what features they have in common
   			Collection<String> interFeatures =
   				new ArrayList<>(FeatureSet.intersect(fsTarget, fsActual).getFeatures());
   			interFeatures.remove(lookFor);
   			
   			// Features in the intersection determine the beginnings and ends
   			// of a consonant harmony
   			for(String feature : interFeatures) {
				// Any feature in the intersection that is in an existing
				// potential result can be removed because this indicates
	   			// an end to the harmony
				Iterator<HarmonyDetectorResult> iter = current.iterator();
   				while(iter.hasNext()) {
   					HarmonyDetectorResult res = iter.next();
   					if(res.getSharedFeatures().hasFeature(feature)) {
   						addResult(res);
   						iter.remove();
   					}
   				}
   				
   				// This feature can be a potential for a new harmony
   				HarmonyDetectorResult res = new HarmonyDetectorResult(map);
   				res.setFirstPosition(index);
   				res.setSecondPosition(index);
   				res.setSharedFeatures(FeatureSet.union(res.getSharedFeatures(), FeatureSet.singleonFeature(feature)));
   				current.add(res);
   			}
   			
   			// Update the end positions of every result that wasn't removed
   			// in the loop above
   			Iterator<HarmonyDetectorResult> iter = current.iterator();
OUTER:		while(iter.hasNext()) {
				HarmonyDetectorResult res = iter.next();
   				
   				// The neutralized features at this point
   	   			FeatureSet neutralized =
   						getNeutralized(fsTarget, fsActual, res.getSharedFeatures());
   				
   				// If there is a result with a feature that did not carry on
   				// in the actual form then it can be removed
   				for(String feat : fsActual.getFeatures()) {
   					if(res.getSharedFeatures().hasFeature(feat)) {
   						// Feature shared, so detect neutralized features
   						if(res.getFirstPosition() != index) {
   							res.setNeutralizedFeatures(
   									FeatureSet.intersect(res.getNeutralizedFeatures(), neutralized) );
   						}
   						
   						// Continue the loop labeled OUTER so that a result is
   						// not added below
   						res.setSecondPosition(index);
   						continue OUTER;
   					}
   				}
   				
   				addResult(res);
   				iter.remove();
   			}
    	}
   		
   		// Add any results that did not get added above (i.e., harmonies
   		// that go right to the end)00
   		for(HarmonyDetectorResult r : current)
   			addResult(r);
    }
    
    /**
     * Gets all of the neutralized features in the target form based on
     * a set of shared features.
     * @param target  the feature set for the target form
     * @param shared  the shared features
     * @return        a collection containing the neutralized features
     */
    public FeatureSet getNeutralized(
    		FeatureSet target,
    		FeatureSet actual,
    		FeatureSet shared)
    {
    	FeatureMatrix featureMat = FeatureMatrix.getInstance();
    	FeatureSet neutralized = new FeatureSet();
    	
    	// Try to find a feature in the same family but was not a
    	// shared feature
    	for(String feature : shared.getFeatures()) {
    		// Get the features in the same family
    		String primaryFamily = featureMat.getFeaturePrimaryFamily(feature);
			Collection<String> primaries =
					featureMat.getFeaturesWithPrimaryFamily(primaryFamily);
			
			// Create a feature set out of them
			HashSet<String> set = new HashSet<String>(primaries);
			FeatureSet setPrimaries = new FeatureSet(set);
			setPrimaries = FeatureSet.intersect(setPrimaries, target);
			setPrimaries = FeatureSet.minus(setPrimaries, actual);

			neutralized = FeatureSet.union(neutralized, setPrimaries);
    	}
    	
    	// Remove any shared features
    	neutralized = FeatureSet.minus(neutralized, shared);
    	return neutralized;
    }
    
    /*
     * A set of implied features of the form {super : implied} so that
     * if `super` is found in the feature set then every feature in
     * `implied` is removed.
     */
    /*private static Hashtable<String, String[]> implications;
    static {
    	implications = new Hashtable<String, String[]>();
    	implications.put("Alveolar", new String[]{"Anterior","Coronal"});
    	implications.put("Lateral", new String[]{"Approximant","Sonorant","Liquid","Continuant"});
    	implications.put("Nasal", new String[]{"Sonorant"});
    	implications.put("Stop", new String[]{"Obstruent"});
    	implications.put("Affricative", new String[]{"Obstruent"});
    	implications.put("Fricative", new String[]{"Obstruent","Continuant"});
    	implications.put("Approximant", new String[]{"Continuant","Sonorant"});
    };*/
    
    /**
     * Adds the result to the list of results. Takes care of merging the
     * result with another, and also discards result if zero length.
     * @param r  the Result to add
     */
    private void addResult(HarmonyDetectorResult r) {
    	// Destroy harmonies that neutralized nothing
    	//if(r.getNeutralizedFeatures().getFeatures().size() == 0) return;
    	if(r.getLength() == 0) return;
    	
    	// If a result already exists at the same positions, it was
    	// already completely taken care of by the code below
    	HarmonyDetectorResult newResult = null;
    	for(DetectorResult res : results) {
    		if(res.getFirstPosition() == r.getFirstPosition() &&
    			res.getSecondPosition() == r.getSecondPosition())
    		{
    			newResult = (HarmonyDetectorResult)res;
    			break;
    		}
    	}
    	
    	// No existing harmony at the same location, so just add this one
    	// to the list
    	if(newResult == null) {
    		results.add(r);
    		return;
    	}
    	
    	// Update existing result with new shared features
    	newResult.setSharedFeatures(
    			FeatureSet.union(newResult.getSharedFeatures(), r.getSharedFeatures()) );
    	
    	// Update neutralized feature set because of the additional
    	// shared features
    	FeatureSet newNeutralized = 
    		FeatureSet.union(newResult.getNeutralizedFeatures(), r.getNeutralizedFeatures());
    	newNeutralized = FeatureSet.minus(newNeutralized, newResult.getSharedFeatures());
    	newResult.setNeutralizedFeatures(newNeutralized);
    }
    
}
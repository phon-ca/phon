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
package ca.phon.query.detectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

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
    	ArrayList<Result> current = new ArrayList<Result>();
    	
    	// The Consonant/Vowel feature depending on the type of detection
    	// specified by the `consonants` variable
    	String lookFor = (consonants ? "Consonant" : "Vowel");
    	
    	// Perform for each feature
    	int len = map.getAlignmentLength();
   		for(int i = 0; i < len; ++i) {
   			// Get aligned pairs
   			int index = (leftToRight ? i : len - i - 1);
   			List<IPAElement> pair1 = map.getAlignedElements(index);
   			if(pair1.get(0) == null || pair1.get(1) == null)
   				continue;
   			
   			// Get feature sets
   			FeatureSet fsTarget = pair1.get(0).getFeatureSet();
   			FeatureSet fsActual = pair1.get(1).getFeatureSet();
   			if(!fsTarget.hasFeature(lookFor) || !fsActual.hasFeature(lookFor))
   				continue;
   			
   			// Find out what features they have in common
   			Collection<String> interFeatures =
   				FeatureSet.intersect(fsTarget, fsActual).getFeatures();
   			interFeatures.remove(lookFor);
   			
   			// Features in the intersection determine the beginnings and ends
   			// of a consonant harmony
   			for(String feature : interFeatures) {
				// Any feature in the intersection that is in an existing
				// potential result can be removed because this indicates
	   			// an end to the harmony
				Iterator<Result> iter = current.iterator();
   				while(iter.hasNext()) {
   					Result res = iter.next();
   					if(res.getSharedFeatures().hasFeature(feature)) {
   						addResult(res);
   						iter.remove();
   					}
   				}
   				
   				// This feature can be a potential for a new harmony
   				Result res = new Result(map);
   				res.setFirstPosition(index);
   				res.setSecondPosition(index);
   				res.getSharedFeatures().addFeature(feature);
   				current.add(res);
   			}
   			
   			// Update the end positions of every result that wasn't removed
   			// in the loop above
   			Iterator<Result> iter = current.iterator();
OUTER:		while(iter.hasNext()) {
   				Result res = iter.next();
   				
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
   		for(Result r : current)
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
    private void addResult(Result r) {
    	// Destroy harmonies that neutralized nothing
    	//if(r.getNeutralizedFeatures().getFeatures().size() == 0) return;
    	if(r.getLength() == 0) return;
    	
    	// If a result already exists at the same positions, it was
    	// already completely taken care of by the code below
    	Result newResult = null;
    	for(DetectorResult res : results) {
    		if(res.getFirstPosition() == r.getFirstPosition() &&
    			res.getSecondPosition() == r.getSecondPosition())
    		{
    			newResult = (Result)res;
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
    
    /**
     * A result from consonant harmony detection.
     *
     * @author  Jason Gedge <gedge@cs.mun.ca>
     */
    public class Result extends DetectorResult {    	
    	// Vowel or consonant harmony
    	private DetectorResultType type;
    	
    	/**
    	 * Default constructor. Uses the consonants member of the parent
    	 * class to set the type of harmony for this result.
    	 */
    	public Result(PhoneMap pm) {
    		super(pm);
    		if(consonants)
    			type = DetectorResultType.ConsonantHarmony;
    		else
    			type = DetectorResultType.VowelHarmony;
    		
    		// Start off with every feature neutralized
    		setNeutralizedFeatures(
    				new FeatureSet(FeatureMatrix.getInstance().getFeatures()));
    	}
    	
    	/*
    	 * DetectorResult implementation
    	 */
        @Override
		public DetectorResultType getType() {
        	return type;
        }
        
        /**
         * Get the length of the harmony.
         * @return  the length
         */
        public int getLength() { return Math.abs(this.pos2 - this.pos1); }

        /**
         * Get the shared features affected by the harmony.
         * @return  the feature
         */
        public FeatureSet getSharedFeatures() { return this.features1; }
        
        /**
         * Set the shared features affected by the harmony.
         * @param features  the new set of features
         */
        public void setSharedFeatures(FeatureSet features) { this.features1 = features; }
        
        /**
         * Get the neutralized features affected by the harmony.
         * @return  the feature
         */
        public FeatureSet getNeutralizedFeatures() { return this.features2; }
        
        /**
         * Set the neutralized features affected by the harmony.
         * @param features  the new set of features
         */
        public void setNeutralizedFeatures(FeatureSet features) { this.features2 = features; }

        /**
         * Check to see if this harmony goes from left to right or from
         * right to left (progressive or regressive).
         * @return  true if left-to-right, false otherwise
         */
        public boolean isLeftToRight() { return (this.pos1 <= this.pos2); }
        
        /**
         * Get the alignment positions of parts that were involved in the
         * harmony.
         * @return  the positions involved in the harmony
         */
        public int[] getPositionsInvolved() {
        	if(map == null || getLength() == 0) return new int[] {};
        	
        	String lookFor = (type == DetectorResultType.VowelHarmony ? "Vowel" : "Consonant");
        	
        	// Create an array big enough to store all of the potential
        	// positions involved
        	int[] tmp = new int[getLength() + 1];
        	
        	int start = Math.min(pos1, pos2);
        	int end = Math.max(pos1, pos2);
        	int j = 0;
        	for(int i = start; i <= end; ++i) {
        		List<IPAElement> p = map.getAlignedElements(i);
        		if(p.get(0) == null || p.get(1) == null) continue;
        		if(!p.get(0).getFeatureSet().hasFeature(lookFor)) continue;
        		if(!p.get(1).getFeatureSet().hasFeature(lookFor)) continue;
        		tmp[j++] = i;
        	}
        	
        	// Copy over result elements
        	int[] result = new int[j];
        	for(int i = 0; i < j; ++i) result[i] = tmp[i];
        	
        	return result;
        }

        /*
         * Object override(s)
         */
        @Override
		public String toString() {
        	if(map == null) return "";
        	
        	final String ELLIPSIS = "\u2026";
        	
        	int pos1 = isLeftToRight() ? this.pos1 : this.pos2;
        	int pos2 = isLeftToRight() ? this.pos2 : this.pos1;
        	List<IPAElement> elems1 = map.getAlignedElements(pos1);
        	List<IPAElement> elems2 = map.getAlignedElements(pos2);
        	
        	// Set up target/actual strings
        	String sTarget = elems1.get(0).toString();
        	String sActual = elems1.get(1).toString();
        	if(pos1 != pos2 - 1) {
        		sTarget += ELLIPSIS;
        		sActual += ELLIPSIS;
        	}
        	sTarget += elems2.get(0).toString();
        	sActual += elems2.get(1).toString();
        	if(pos1 > 0) {
        		sTarget = ELLIPSIS + sTarget;
        		sActual = ELLIPSIS + sActual;
        	}
        	if(pos2 < map.getAlignmentLength() - 1) {
        		sTarget = sTarget + ELLIPSIS;
        		sActual = sActual + ELLIPSIS;
        	}
        	
        	return String.format(
        			"%s \u2192 %s",
        			sTarget, sActual);
        }
    }
}
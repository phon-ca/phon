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
import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;

public class HarmonyDetectorResult extends DetectorResult {

	private FeatureSet sharedFeatures = new FeatureSet();
	
	private FeatureSet neutralizedFeatures = new FeatureSet();

	private PhoneticProfile sharedProfile = new PhoneticProfile();
	
	private PhoneticProfile neutralizedProfile = new PhoneticProfile();
	
	public HarmonyDetectorResult(PhoneMap phoneMap) {
		super(phoneMap);
	}
	
	/**
     * Get the length of the harmony.
     * @return  the length
     */
    public int getLength() { return Math.abs(this.pos2 - this.pos1); }

    /**
     * Get the shared profile for the harmony    
     */
    public PhoneticProfile getSharedProfile() {
    	return this.sharedProfile;
    }
    
    /**
     * Set the shared profile for the harmony
     * 
     * @param profile
     */
    public void setSharedProfile(PhoneticProfile profile) {
    	this.sharedProfile = profile;
    }
    
    /**
     * Get neutralized profile for harmony 
     * 
     * @return
     */
    public PhoneticProfile getNeutralizedProfile() {
    	return this.neutralizedProfile;
    }
    
    /**
     * Set neutralized profile for harmony
     * 
     * @param profile
     */
    public void setNeutralizedProfile(PhoneticProfile profile) {
    	if(profile == null) 
    		System.out.println("Hello world");
    	this.neutralizedProfile = profile;
    }
    
    /**
     * Get the shared features affected by the harmony.
     * @return  the feature
     */
    @Deprecated
    public FeatureSet getSharedFeatures() { return this.sharedFeatures; }
    
    /**
     * Set the shared features affected by the harmony.
     * @param features  the new set of features
     */
    @Deprecated
    public void setSharedFeatures(FeatureSet features) { this.sharedFeatures = features; }
    
    /**
     * Get the neutralized features affected by the harmony.
     * @return  the feature
     */
    @Deprecated
    public FeatureSet getNeutralizedFeatures() { return this.neutralizedFeatures; }
    
    /**
     * Set the neutralized features affected by the harmony.
     * @param features  the new set of features
     */
    @Deprecated
    public void setNeutralizedFeatures(FeatureSet features) { this.neutralizedFeatures = features; }

    /**
     * Check to see if this harmony goes from left to right or from
     * right to left (progressive or regressive).
     * @return  true if left-to-right, false otherwise
     */
    public boolean isLeftToRight() { return (this.pos1 <= this.pos2); }
    
//    /**
//     * Get the alignment positions of parts that were involved in the
//     * harmony.
//     * @return  the positions involved in the harmony
//     */
//    public int[] getPositionsInvolved() {
//    	if(getPhoneMap() == null || getLength() == 0) return new int[] {};
//    	
////    	String lookFor = (type == DetectorResultType.VowelHarmony ? "Vowel" : "Consonant");
//    	
//    	// Create an array big enough to store all of the potential
//    	// positions involved
//    	int[] tmp = new int[getLength() + 1];
//    	
//    	int start = Math.min(pos1, pos2);
//    	int end = Math.max(pos1, pos2);
//    	int j = 0;
//    	for(int i = start; i <= end; ++i) {
//    		List<IPAElement> p = map.getAlignedElements(i);
//    		if(p.get(0) == null || p.get(1) == null) continue;
//    		if(!p.get(0).getFeatureSet().hasFeature(lookFor)) continue;
//    		if(!p.get(1).getFeatureSet().hasFeature(lookFor)) continue;
//    		tmp[j++] = i;
//    	}
//    	
//    	// Copy over result elements
//    	int[] result = new int[j];
//    	for(int i = 0; i < j; ++i) result[i] = tmp[i];
//    	
//    	return result;
//    }

    /*
     * Object override(s)
     */
    @Override
	public String toString() {
    	if(getPhoneMap() == null) return "";
    	
    	final String ELLIPSIS = "\u2026";
    	
    	int pos1 = isLeftToRight() ? this.pos1 : this.pos2;
    	int pos2 = isLeftToRight() ? this.pos2 : this.pos1;
    	List<IPAElement> elems1 = getPhoneMap().getAlignedElements(pos1);
    	List<IPAElement> elems2 = getPhoneMap().getAlignedElements(pos2);
    	
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
    	if(pos2 < getPhoneMap().getAlignmentLength() - 1) {
    		sTarget = sTarget + ELLIPSIS;
    		sActual = sActual + ELLIPSIS;
    	}
    	
    	return String.format(
    			"%s \u2192 %s",
    			sTarget, sActual);
    }
	
}

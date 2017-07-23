package ca.phon.query.detectors;

import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipa.features.FeatureSet;

public class HarmonyDetectorResult extends DetectorResult {

	private FeatureSet sharedFeatures;
	
	private FeatureSet neutralizedFeatures;

	public HarmonyDetectorResult(PhoneMap phoneMap) {
		super(phoneMap);
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
    public FeatureSet getSharedFeatures() { return this.sharedFeatures; }
    
    /**
     * Set the shared features affected by the harmony.
     * @param features  the new set of features
     */
    public void setSharedFeatures(FeatureSet features) { this.sharedFeatures = features; }
    
    /**
     * Get the neutralized features affected by the harmony.
     * @return  the feature
     */
    public FeatureSet getNeutralizedFeatures() { return this.neutralizedFeatures; }
    
    /**
     * Set the neutralized features affected by the harmony.
     * @param features  the new set of features
     */
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

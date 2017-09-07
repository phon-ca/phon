package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;

/**
 * 
 */
public class HarmonyDetector extends BasicHarmonyDetector {

	private boolean includePlace = true;

	private boolean includeManner = true;

	private boolean includeVoicing = true;

	private boolean includeHeight = true;
	
	private boolean includeBackness = true;
	
	private boolean includeTenseness = true;
	
	private boolean includeRounding = true;

	public HarmonyDetector(boolean consonants) {
		super(consonants);
	}
	
	public HarmonyDetector(boolean consonants, boolean includePlace, boolean includeManner, boolean includeVoicing,
			boolean includeHeight, boolean includeBackness, boolean includeTenseness, boolean includeRounding) {
		super(consonants);
		this.includePlace = includePlace;
		this.includeManner = includeManner;
		this.includeVoicing = includeVoicing;
		this.includeHeight = includeHeight;
		this.includeBackness = includeBackness;
		this.includeTenseness = includeTenseness;
		this.includeRounding = includeRounding;
	}

	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> allPossibleResults = super.detect(pm);
		final List<DetectorResult> results = new ArrayList<>();
	
		for(DetectorResult possibleResult:allPossibleResults) {
			if(isConsonants() && isConsonantHarmony((HarmonyDetectorResult)possibleResult)) {
				results.add(possibleResult);
			} else if(isVowelHarmony((HarmonyDetectorResult)possibleResult)){
				results.add(possibleResult);
			}
		}
		
		return results;
	}
	
	/**
	 * Determine if there is consonant harmony.  Will also update the shared and neutralized
	 * {@link PhoneticProfile} for the result.
	 * 
	 * @param potentialResult
	 * @return
	 */
	public boolean isConsonantHarmony(HarmonyDetectorResult potentialResult) {
		List<PhoneDimension> dimensions = new ArrayList<>();
		if(includePlace) dimensions.add(PhoneDimension.PLACE);
		if(includeManner) dimensions.add(PhoneDimension.MANNER);
		if(includeVoicing) dimensions.add(PhoneDimension.VOICING);
		
		return isHarmony(potentialResult, dimensions);
	}
	
	public boolean isVowelHarmony(HarmonyDetectorResult potentialResult) {
		List<PhoneDimension> dimensions = new ArrayList<>();
		if(includeHeight) dimensions.add(PhoneDimension.HEIGHT);
		if(includeBackness) dimensions.add(PhoneDimension.BACKNESS);
		if(includeTenseness) dimensions.add(PhoneDimension.TENSENESS);
		if(includeRounding) dimensions.add(PhoneDimension.ROUNDING);
		
		return isHarmony(potentialResult, dimensions);
	}
	
	private boolean isHarmony(HarmonyDetectorResult potentialResult, List<PhoneDimension> dimensions) {
		PhoneMap pm = potentialResult.getPhoneMap();
		int p1 = potentialResult.getFirstPosition();
		int p2 = potentialResult.getSecondPosition();
		
		PhoneticProfile t1Profile = 
				(p1 >= 0 ? new PhoneticProfile(pm.getTopAlignmentElements().get(p1)) : new PhoneticProfile());
		PhoneticProfile t2Profile = 
				(p2 >= 0 ? new PhoneticProfile(pm.getTopAlignmentElements().get(p2)) : new PhoneticProfile());
		
		PhoneticProfile a1Profile = 
				(p1 >= 0 ? new PhoneticProfile(pm.getBottomAlignmentElements().get(p1)) : new PhoneticProfile());
		PhoneticProfile a2Profile = 
				(p2 >= 0 ? new PhoneticProfile(pm.getBottomAlignmentElements().get(p2)) : new PhoneticProfile());
		
		PhoneticProfile sharedProfile = new PhoneticProfile();
		PhoneticProfile neutralizedProfile = new PhoneticProfile();
		
		boolean hasHarmony = false;
		for(PhoneDimension dimension:dimensions) {
			hasHarmony |= 
					checkHarmony(dimension, sharedProfile, neutralizedProfile, t1Profile, t2Profile, a1Profile, a2Profile);
		}
		
		potentialResult.setSharedProfile(sharedProfile);
		potentialResult.setNeutralizedProfile(neutralizedProfile);
		
		return hasHarmony;
	}
	
	private boolean checkHarmony(PhoneDimension dimension, 
			/*out*/ PhoneticProfile sharedProfile, /*out*/ PhoneticProfile neutralizedProfile,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile) {
		Integer t1Val = t1Profile.getProfile().get(dimension);
		Integer t2Val = t2Profile.getProfile().get(dimension);
		Integer a1Val = a1Profile.getProfile().get(dimension);
		Integer a2Val = a2Profile.getProfile().get(dimension);
		
		// target categories must be different, actual values the same
		if( (t1Val != t2Val) && (a1Val == a2Val) && (a1Val == t1Val)) {
			// update profile
			sharedProfile.put(dimension, t1Profile.getProfile().get(dimension));
			neutralizedProfile.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}
		
		return false;
	}
}

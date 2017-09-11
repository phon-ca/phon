package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.PhoneMap;

public class MetathesisDetector extends BasicMetathesisDetector {

	private boolean includePlace = true;

	private boolean includeManner = true;

	private boolean includeVoicing = true;

	public MetathesisDetector() {
	}
	
	public MetathesisDetector(boolean includePlace, boolean includeManner, boolean includeVoicing) {
		super();
		this.includePlace = includePlace;
		this.includeManner = includeManner;
		this.includeVoicing = includeVoicing;
	}

	public boolean isIncludePlace() {
		return includePlace;
	}

	public void setIncludePlace(boolean includePlace) {
		this.includePlace = includePlace;
	}

	public boolean isIncludeManner() {
		return includeManner;
	}

	public void setIncludeManner(boolean includeManner) {
		this.includeManner = includeManner;
	}

	public boolean isIncludeVoicing() {
		return includeVoicing;
	}

	public void setIncludeVoicing(boolean includeVoicing) {
		this.includeVoicing = includeVoicing;
	}

	@Override
	public Collection<DetectorResult> detect(PhoneMap pm) {
		final Collection<DetectorResult> allPossibleResults = super.detect(pm);
		final List<DetectorResult> results = new ArrayList<>();
	
		for(DetectorResult possibleResult:allPossibleResults) {
			if(isMetathesis((MetathesisDetectorResult)possibleResult)) {
				results.add(possibleResult);
			}
		}
		
		return results;
	}
	
	public boolean isMetathesis(MetathesisDetectorResult potentialResult) {
		List<PhoneDimension> dimensions = new ArrayList<>();
		if(includePlace) dimensions.add(PhoneDimension.PLACE);
		if(includeManner) dimensions.add(PhoneDimension.MANNER);
		if(includeVoicing) dimensions.add(PhoneDimension.VOICING);
		
		return isMetathesis(potentialResult, dimensions);
	}
	
	private boolean isMetathesis(MetathesisDetectorResult potentialResult, List<PhoneDimension> dimensions) {
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
		
		PhoneticProfile profile1 = new PhoneticProfile();
		PhoneticProfile profile2 = new PhoneticProfile();
		
		boolean hasMetathesis = false;
		for(PhoneDimension dimension:dimensions) {
			hasMetathesis |=
					checkHarmony(dimension, profile1, profile2, t1Profile, t2Profile, a1Profile, a2Profile);
		}
		
		potentialResult.setProfile1(profile1);
		potentialResult.setProfile2(profile2);
		
		return hasMetathesis;
	}
	
	private boolean checkHarmony(PhoneDimension dimension, 
			/*out*/ PhoneticProfile profile1, /*out*/ PhoneticProfile profile2,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile) {
		Integer t1Val = t1Profile.getProfile().get(dimension);
		Integer t2Val = t2Profile.getProfile().get(dimension);
		Integer a1Val = a1Profile.getProfile().get(dimension);
		Integer a2Val = a2Profile.getProfile().get(dimension);
		
		if(t1Val != t2Val && t1Val == a2Val && t2Val == a1Val) {
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, a1Profile.getProfile().get(dimension));
			return true;
		}
		
		return false;
	}
	
}

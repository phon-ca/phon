package ca.phon.ipa.relations;


import ca.phon.ipa.*;
import ca.phon.ipa.relations.SegmentalRelation.Relation;

public class HarmonyDetector extends AbstractSegmentalRelationDetector {
	
	public HarmonyDetector() {
		super(Relation.Harmony, true, false, true);
	}
	
	@Override
	protected boolean checkRelation(PhoneDimension dimension, 
			/*out*/ PhoneticProfile sharedProfile, /*out*/ PhoneticProfile neutralizedProfile,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile) {
		Integer t1Val = t1Profile.getProfile().get(dimension);
		Integer t2Val = t2Profile.getProfile().get(dimension);
		Integer a1Val = a1Profile.getProfile().get(dimension);
		Integer a2Val = a2Profile.getProfile().get(dimension);
		
		// target categories must be different, actual values the same
		if( (t1Val != t2Val) && (t2Val != null) && (a1Val == a2Val) && (a1Val == t1Val)) {
			// update profile
			sharedProfile.put(dimension, t1Profile.getProfile().get(dimension));
			neutralizedProfile.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}
		
		return false;
	}

}

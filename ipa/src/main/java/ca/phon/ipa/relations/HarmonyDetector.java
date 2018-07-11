package ca.phon.ipa.relations;


import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.PhoneticProfile;
import ca.phon.ipa.features.FeatureSet;
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
		FeatureSet t1Val = t1Profile.getProfile().get(dimension);
		FeatureSet t2Val = t2Profile.getProfile().get(dimension);
		FeatureSet a1Val = a1Profile.getProfile().get(dimension);
		FeatureSet a2Val = a2Profile.getProfile().get(dimension);

		// target categories must be different, actual values the same
		if( (!t1Val.equals(t2Val)) && (t2Val.size() > 0) && (a1Val.equals(a2Val)) && (a1Val.equals(t1Val)) ) {
			// update profile
			sharedProfile.put(dimension, t1Profile.getProfile().get(dimension));
			neutralizedProfile.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}

		return false;
	}

}

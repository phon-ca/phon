package ca.phon.ipa.relations;


import ca.phon.ipa.*;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.relations.SegmentalRelation.Relation;

public class AssimilationDetector extends AbstractSegmentalRelationDetector {

	public AssimilationDetector() {
		super(Relation.Assimilation, true, true, false);
	}

	@Override
	protected boolean checkRelation(PhoneDimension dimension, PhoneticProfile profile1, PhoneticProfile profile2,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile, PhoneticProfile a1Profile,
			PhoneticProfile a2Profile) {
		FeatureSet t1Val = t1Profile.getProfile().get(dimension);
		FeatureSet t2Val = t2Profile.getProfile().get(dimension);
		FeatureSet a1Val = a1Profile.getProfile().get(dimension);
		FeatureSet a2Val = a2Profile.getProfile().get(dimension);

		// target categories must be different, actual values the same
		if( (!t1Val.equals(t2Val)) && (t2Val.size() > 0) && (a1Val.equals(a2Val)) && (a1Val.equals(t1Val)) ) {
			// update profile
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}

		return false;
	}

}

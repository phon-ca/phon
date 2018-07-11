package ca.phon.ipa.relations;

import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.PhoneticProfile;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.relations.SegmentalRelation.Relation;

public class MigrationDetector extends AbstractSegmentalRelationDetector {

	public MigrationDetector() {
		super(Relation.Migration, true, false, true);
	}

	@Override
	protected boolean checkRelation(PhoneDimension dimension,
			/*out*/ PhoneticProfile profile1, /*out*/ PhoneticProfile profile2,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile) {
		FeatureSet t1Val = t1Profile.getProfile().get(dimension);
		FeatureSet t2Val = t2Profile.getProfile().get(dimension);
		FeatureSet a1Val = a1Profile.getProfile().get(dimension);
		FeatureSet a2Val = a2Profile.getProfile().get(dimension);

		// target categories must be different, actual values the same
		if( (!t1Val.equals(t2Val)) &&
				(a1Val.size() == 0) && (a2Val.equals(t1Val)) ) {
			// update profile
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}

		return false;
	}

}

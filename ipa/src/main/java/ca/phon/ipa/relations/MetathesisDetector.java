package ca.phon.ipa.relations;

import ca.phon.ipa.PhoneDimension;
import ca.phon.ipa.PhoneticProfile;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.relations.SegmentalRelation.Relation;

public class MetathesisDetector extends AbstractSegmentalRelationDetector {

	public MetathesisDetector() {
		super(Relation.Metathesis, false, true, true);
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

		boolean isMetathesis = ( (t1Val.size() > 0) && (t2Val.size() > 0) &&
				(!t1Val.equals(t2Val)) && (t1Val.equals(a2Val)) && (t2Val.equals(a1Val)) );
		if(isMetathesis) {
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, a1Profile.getProfile().get(dimension));
		}

		return isMetathesis;
	}

}

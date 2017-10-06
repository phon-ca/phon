package ca.phon.ipa.relations;

import ca.phon.ipa.*;
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
		Integer t1Val = t1Profile.getProfile().get(dimension);
		Integer t2Val = t2Profile.getProfile().get(dimension);
		Integer a1Val = a1Profile.getProfile().get(dimension);
		Integer a2Val = a2Profile.getProfile().get(dimension);
		
		// target categories must be different, actual values the same
		if( (t1Val != t2Val) &&
				(a1Val == null) && (a2Val == t1Val) ) {
			// update profile
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, t2Profile.getProfile().get(dimension));
			return true;
		}
		
		return false;
	}

}

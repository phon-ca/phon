package ca.phon.ipa.relations;

import ca.phon.ipa.*;
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
		Integer t1Val = t1Profile.getProfile().get(dimension);
		Integer t2Val = t2Profile.getProfile().get(dimension);
		Integer a1Val = a1Profile.getProfile().get(dimension);
		Integer a2Val = a2Profile.getProfile().get(dimension);
		
		boolean isMetathesis = ( (t1Val != null) && (t2Val != null) &&
				(t1Val != t2Val) && (t1Val == a2Val) && (t2Val == a1Val));
		
		if(isMetathesis) {
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, a1Profile.getProfile().get(dimension));
		}
		
		return isMetathesis;
	}
	
}

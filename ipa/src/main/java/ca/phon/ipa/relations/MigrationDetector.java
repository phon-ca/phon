/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.ipa.relations;

import ca.phon.ipa.*;
import ca.phon.ipa.features.*;
import ca.phon.ipa.relations.SegmentalRelation.*;

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

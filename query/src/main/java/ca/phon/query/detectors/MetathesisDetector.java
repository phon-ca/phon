/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;
import ca.phon.query.detectors.MetathesisDetectorResult.*;

/**
 * <div id='metathesis'><h2>Metathesis</h2>
 *
 * <p>Given two positions <i>i</i>, <i>k</i> within M, we determine if metathesis exists for each dimension of <i>profile(p)</i> if any of the following cases are true:
 * <ul>
 * 	<li>
 * 		<b>Metathesis</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>y</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>y</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>x</i> (Values from A)<br/>
 * 	</li>
 * 	<li>
 * 		<b>Progressive Migration</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>y</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>?</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>x</i> (Values from A)<br/>
 * 	</li>
 * 	<li>
 * 		<b>Regressive Migration</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>y</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>y</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>?</i> (Values from A)<br/>
 * 	</li>
 * </ul>
 * </p>
 */
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
			Type type =
					checkHarmony(dimension, profile1, profile2, t1Profile, t2Profile, a1Profile, a2Profile);
			potentialResult.setType(dimension, type);
			hasMetathesis |= (type.ordinal() != Type.Undefined.ordinal());
		}

		potentialResult.setProfile1(profile1);
		potentialResult.setProfile2(profile2);

		return hasMetathesis;
	}

	private MetathesisDetectorResult.Type checkHarmony(PhoneDimension dimension,
			/*out*/ PhoneticProfile profile1, /*out*/ PhoneticProfile profile2,
			PhoneticProfile t1Profile, PhoneticProfile t2Profile,
			PhoneticProfile a1Profile, PhoneticProfile a2Profile) {
		Type retVal = Type.Undefined;

		FeatureSet t1Val = t1Profile.getProfile().get(dimension);
		FeatureSet t2Val = t2Profile.getProfile().get(dimension);
		FeatureSet a1Val = a1Profile.getProfile().get(dimension);
		FeatureSet a2Val = a2Profile.getProfile().get(dimension);

		boolean isMetathesis = (t1Val != t2Val && t1Val == a2Val && t2Val == a1Val);
		boolean isProgressiveMigration = (t1Val != t2Val && a1Val != a2Val && a2Val == t1Val);
		boolean isRegressiveMigraion =  (t1Val != t2Val && a1Val != a2Val && a1Val == t2Val);

		if(isMetathesis || isProgressiveMigration || isRegressiveMigraion) {
			profile1.put(dimension, t1Profile.getProfile().get(dimension));
			profile2.put(dimension, a1Profile.getProfile().get(dimension));

			if(isMetathesis) retVal = Type.Metathesis;
			else if(isProgressiveMigration) retVal = Type.ProgressiveMigration;
			else if(isRegressiveMigraion) retVal = Type.RegressiveMigration;
		}

		return retVal;
	}

}

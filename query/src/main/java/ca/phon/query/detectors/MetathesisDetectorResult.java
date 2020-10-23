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
package ca.phon.query.detectors;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.ipa.features.*;

public class MetathesisDetectorResult extends DetectorResult {
	
	public static enum Type {
		Undefined("Undefined"),
		Metathesis("Metathesis"),
		ProgressiveMigration("Progressive Migration"),
		RegressiveMigration("Regressive Migration");
		
		private String label;
		
		private Type(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	};
	
	private PhoneticProfile profile1 = new PhoneticProfile();
	
	private PhoneticProfile profile2 = new PhoneticProfile();
	
	private FeatureSet features1;
	
	private FeatureSet features2;
	
	private Map<PhoneDimension, Type> typeMap = new LinkedHashMap<>();

	public MetathesisDetectorResult(PhoneMap phoneMap) {
		super(phoneMap);
	}
	
	public Type getType(PhoneDimension dim) {
		return this.typeMap.get(dim);
	}
	
	public void setType(PhoneDimension dim, Type type) {
		this.typeMap.put(dim, type);
	}
	
	public PhoneticProfile getProfile1() {
		return profile1;
	}

	public void setProfile1(PhoneticProfile profile1) {
		this.profile1 = profile1;
	}

	public PhoneticProfile getProfile2() {
		return profile2;
	}

	public void setProfile2(PhoneticProfile profile2) {
		this.profile2 = profile2;
	}

	@Deprecated
	public FeatureSet getFeatures1() {
		return features1;
	}

	@Deprecated
	public void setFeatures1(FeatureSet features1) {
		this.features1 = features1;
	}

	@Deprecated
	public FeatureSet getFeatures2() {
		return features2;
	}

	@Deprecated
	public void setFeatures2(FeatureSet features2) {
		this.features2 = features2;
	}
	
	@Override
	public String toString() {
		final PhoneMap map = getPhoneMap();
		if(map == null) return "";

		final String ELLIPSIS = "\u2026";
		List<IPAElement> elems1 = map.getAlignedElements(this.pos1);
		List<IPAElement> elems2 = map.getAlignedElements(this.pos2);

		// Set up target string
		String sTarget = (elems1.get(0) != null
				? elems1.get(0).toString()
						: " ");
		if(pos1 != pos2 - 1) sTarget += ELLIPSIS;
		sTarget += (elems2.get(0) != null
				? elems2.get(0).toString()
						: " ");
		if(pos1 > 0) sTarget = ELLIPSIS + sTarget;
		if(pos2 < map.getAlignmentLength() - 1) sTarget = sTarget + ELLIPSIS;

		// Set up actual string
		String sActual = (elems1.get(1) != null
				? elems1.get(1).toString()
						: " ");
		if(pos1 != pos2 - 1) sActual += ELLIPSIS;
		sActual += (elems2.get(1) != null
				? elems2.get(1).toString()
						: " ");
		if(pos1 > 0) sActual = ELLIPSIS + sActual;
		if(pos2 < map.getAlignmentLength() - 1) sActual = sActual + ELLIPSIS;

		return String.format(
				"%s \u2192 %s",
				sTarget, sActual);
	}
	
}

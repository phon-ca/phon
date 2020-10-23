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
package ca.phon.syllabifier.phonex;

import ca.phon.ipa.*;
import ca.phon.phonex.*;

/**
 * Phone matcher for sonority distance.
 *
 */
public class SonorityDistancePhoneMatcher implements PhoneMatcher {
	
	/**
	 * Minimum distance
	 */
	private int minDistance;
	
	/**
	 * Allow flat sonority? (default:false)
	 */
	private boolean allowFlat;

	public SonorityDistancePhoneMatcher() {
		this(0, false);
	}
	
	public SonorityDistancePhoneMatcher(int min) {
		this(min, false);
	}
	
	public SonorityDistancePhoneMatcher(int min, boolean allowFlat) {
		this.minDistance = min;
		this.allowFlat = allowFlat;
	}
	
	@Override
	public boolean matches(IPAElement p) {
		boolean retVal = false;
		
		int distance = 0;
		SonorityInfo info = p.getExtension(SonorityInfo.class);
		if(info != null) {
			distance = info.getDistance();
		}
		if(Math.abs(distance) > 0) {
			if(this.minDistance < 0) {
				retVal = distance <= this.minDistance;
			} else {
				retVal = distance >= this.minDistance;
			}
		} else {
			retVal = allowFlat;
		}
		
		return retVal;
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}

}

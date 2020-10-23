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
package ca.phon.ipa.alignment.pmlu;

import java.lang.ref.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.phonex.*;

/**
 * Implements "Phonological Mean Length of Utterance" (Ingram 2002)
 * calculation for {@link PhoneMap}s.
 */
@Extension(PhoneMap.class)
public class PMLU {

	private final WeakReference<PhoneMap> mapRef;
	
	public PMLU(PhoneMap phoneMap) {
		this.mapRef = new WeakReference<PhoneMap>(phoneMap);
	}
	
	public PhoneMap getPhoneMap() {
		return mapRef.get();
	}
	
	/**
	 * Calculate target PMLU. PMLU is calculated as the number of segments
	 * in the target transcript plus the number of consonants.
	 * 
	 * @return target PMLU
	 */
	public int targetPMLU() {
		final PhoneMap pm = getPhoneMap();
		final IPATranscript target = pm.getTargetRep();
		
		int pmlu = target.removePunctuation(true).length();
		final PhonexPattern pattern = PhonexPattern.compile("\\c");
		final PhonexMatcher matcher = pattern.matcher(target);
		while(matcher.find()) ++pmlu;
		
		return pmlu;
	}
	
	/**
	 * Calculate actual PMLU. PMLU is calculated as the number of segments
	 * in the actual transcript plus number of target-like consonants.
	 * 
	 * @return actual PMLU
	 */
	public int actualPMLU() {
		final PhoneMap pm = getPhoneMap();
		
		int pmlu = 0;
		for(int i = 0; i < pm.getAlignmentLength(); i++) {
			final IPAElement targetEle = pm.getTopAlignmentElements().get(i);
			final IPAElement actualEle = pm.getBottomAlignmentElements().get(i);
			
			if(targetEle != null) {
				// we have a target element...
				if(actualEle != null) {
					// increment pmlu since we have a valid segment
					++pmlu;
				} else {
					continue;
				}
				if(targetEle.getFeatureSet().hasFeature("Consonant")) {
					// check for target-like conditions, in this case target-like means
					// both base glyphs match
					if(actualEle.getFeatureSet().hasFeature("Consonant")) {
						final Phone tPhone = (Phone)targetEle;
						final Phone aPhone = (Phone)actualEle;
						
						// we have a target-like consonant, increment pmlu
						if(tPhone.getBase().equals(aPhone.getBase())) {
							++pmlu;
						}
					}
				}
			}
			// added segments are ignored...
		}
		
		return pmlu;
	}
	
	/**
	 * Calculate PWP = actualPMLU()/targetPMLU()
	 * 
	 * @return pwp
	 */
	public float PWP() {
		return (float)actualPMLU()/(float)targetPMLU();
	}
	
}

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
package ca.phon.ipa.alignment;

import java.util.*;

import ca.phon.alignment.*;
import ca.phon.ipa.*;

public class SyllableAligner extends IndelAligner<IPATranscript> {
	
	private PhoneMap phoneAlignment;
	
	public SyllableAligner() {
		
	}

	@Override
	protected int costSubstitute(IPATranscript t1, IPATranscript t2) {
		int tally = 0;

		final PhoneMap alignment = getPhoneAlignment();
		for(IPAElement ele:t2.audiblePhones()) {
			final List<IPAElement> alignedEle = alignment.getAligned(new IPAElement[]{ele});
			if(alignedEle.size() > 0 && t1.indexOf(alignedEle.get(0)) >= 0) {
				if(ele.getFeatureSet().hasFeature("Consonant")) {
					tally += 2;
				} else if(ele.getFeatureSet().hasFeature("Vowel")) {
					tally += 4;
				} else {
					// we are probably aligning cover symbols
					// hard-align like symbols
					if(alignedEle.get(0).getText().equals(ele.getText())) {
						tally += 4;
					} else {
						tally += 1;
					}
				}
			}
		}
		
		return tally;
	}

	@Override
	protected int costSkip(IPATranscript t) {
		return 1;
	}
	
	public PhoneMap getPhoneAlignment() {
		return this.phoneAlignment;
	}
	
	public void setPhoneAlignment(PhoneMap phoneAlignment) {
		this.phoneAlignment = phoneAlignment;
	}

	public SyllableMap calculateSyllableAlignment(IPATranscript ipaTarget, IPATranscript ipaActual, PhoneMap phoneMap) {
		setPhoneAlignment(phoneMap);
		
		final IPATranscript[] targetSylls = ipaTarget.syllables().toArray(new IPATranscript[0]);
		final IPATranscript[] actualSylls = ipaActual.syllables().toArray(new IPATranscript[0]);
		
		final AlignmentMap<IPATranscript> alignment = calculateAlignment(targetSylls, actualSylls);
		
		final SyllableMap retVal = new SyllableMap(ipaTarget, ipaActual, phoneMap);
		retVal.setTopElements(targetSylls);
		retVal.setBottomElements(actualSylls);
		retVal.setTopAlignment(alignment.getTopAlignment());
		retVal.setBottomAlignment(alignment.getBottomAlignment());
		
		return retVal;
	}
	
}

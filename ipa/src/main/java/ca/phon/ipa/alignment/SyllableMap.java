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
package ca.phon.ipa.alignment;

import ca.phon.alignment.*;
import ca.phon.ipa.*;

/**
 *
 */
public class SyllableMap extends AlignmentMap<IPATranscript> {
	
	/** The target phonetic rep */
	private IPATranscript targetRep;
	/** The actual phonetic rep */
	private IPATranscript actualRep;
	
	private PhoneMap phoneAlignment;
	
	/**
	 * Constructor
	 */
	public SyllableMap(IPATranscript targetRep, IPATranscript actualRep, PhoneMap phoneMap) {
		super();
		
		setTargetRep(targetRep);
		setActualRep(actualRep);
		setPhoneAlignment(phoneMap);
	}
	
	public void setPhoneAlignment(PhoneMap phoneMap) {
		this.phoneAlignment = phoneMap;
	}
	
	public PhoneMap getPhoneAlignment() {
		return this.phoneAlignment;
	}
	
	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
		this.bottomElements = actualRep.syllables().toArray(new IPATranscript[0]);
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
		this.topElements = targetRep.syllables().toArray(new IPATranscript[0]);
	}
	
}

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
package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.Orthography;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.tierdata.TierData;

/**
 * Tier descriptions for default tiers.
 */
public enum SystemTierType {
	Orthography("Orthography", Orthography.class, false),
	/**
	 * word segment information, each word in orthography will be reproduced along with an
	 * internal-media element, this tier is not directly editable.  Tier name comes
	 * from CLAN
	 */
	Wor("%wor", Orthography.class, true),
	IPATarget("IPA Target", IPATranscript.class, false),
	TargetSyllables("Target Syllables", IPATranscript.class, true),
	IPAActual("IPA Actual", IPATranscript.class, false),
	ActualSyllables("Actual Syllables", IPATranscript.class, true),
	PhoneAlignment("Alignment", PhoneAlignment.class, true),
	Segment("Segment", MediaSegment.class, true),
	Notes("Notes", TierData.class, false);
	
	private String tierName;
	
	private Class<?> type;

	private boolean hiddenTier;

	private TierAlignmentRules tierAlignmentRules;
	
	private SystemTierType(String tierName, Class<?> type, boolean hiddenTier) {
		this.tierName = tierName;
		this.type = type;
		this.hiddenTier = hiddenTier;
	}
	
	public static boolean isSystemTier(String tierName) {
		return tierFromString(tierName) != null;
	}
	
	public static SystemTierType tierFromString(String tierName) {
		for(SystemTierType t:values()) {
			if(t.getName().equals(tierName))
				return t;
		}
		
		return null;
	}

	public String getName() {
		return this.tierName;
	}

	public boolean isHiddenTier() {
		return this.hiddenTier;
	}

	public Class<?> getDeclaredType() {
		return this.type;
	}
	
}

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
import ca.phon.orthography.mor.MorTierData;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.session.tierdata.TierData;

/**
 * Tier descriptions for default tiers.
 */
public enum SystemTierType {
	Orthography("Orthography", "*<ID>", Orthography.class, false),
	IPATarget("IPA Target", "%mod",IPATranscript.class, false),
	TargetSyllables("Target Syllables", "%xmodsyll", IPATranscript.class, true),
	IPAActual("IPA Actual", "%pho", IPATranscript.class, false),
	ActualSyllables("Actual Syllables",  "%xphosyll", IPATranscript.class, true),
	PhoneAlignment("Alignment", "%xphoalin", PhoneAlignment.class, true),
	Segment("Segment", "%xseg", MediaSegment.class, true),
	@Deprecated
	Notes("Notes", "%xNotes", TierData.class, false);
	
	private String tierName;

	private String chatTierName;
	
	private Class<?> type;

	private boolean hiddenTier;

	private TierAlignmentRules tierAlignmentRules;
	
	private SystemTierType(String tierName, String chatTierName, Class<?> type, boolean hiddenTier) {
		this.tierName = tierName;
		this.chatTierName = chatTierName;
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

	public String getChatTierName() {
		return this.chatTierName;
	}

	public boolean isHiddenTier() {
		return this.hiddenTier;
	}

	public Class<?> getDeclaredType() {
		return this.type;
	}
	
}

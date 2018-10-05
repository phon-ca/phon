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
package ca.phon.session;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.Orthography;

/**
 * Tier descriptions for default tiers.
 */
public enum SystemTierType implements TierDescription {
	Orthography("Orthography", true, Orthography.class),
	IPATarget("IPA Target", true, IPATranscript.class),
	TargetSyllables("Target Syllables", true, IPATranscript.class),
	IPAActual("IPA Actual", true, IPATranscript.class),
	ActualSyllables("Actual Syllables", true, IPATranscript.class),
	SyllableAlignment("Alignment", true, PhoneMap.class),
	Segment("Segment", false, MediaSegment.class),
	Notes("Notes", false, TierString.class);
	
	private String tierName;
	
	private boolean grouped = false;
	
	private Class<?> type;
	
	private SystemTierType(String tierName, boolean grouped, Class<?> type) {
		this.tierName = tierName;
		this.grouped = grouped;
		this.type = type;
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
	
	@Override
	public boolean isGrouped() {
		return this.grouped;
	}

	@Override
	public String getName() {
		return this.tierName;
	}

	@Override
	public Class<?> getDeclaredType() {
		return this.type;
	}
}

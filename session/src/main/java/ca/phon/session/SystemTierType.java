/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	Notes("Notes", false, String.class);
	
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

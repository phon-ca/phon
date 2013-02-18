/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

public enum SystemTierType {
	Orthography("Orthography"),
	IPATarget("IPA Target"),
	TargetSyllables("Target Syllables"),
	IPAActual("IPA Actual"),
	ActualSyllables("Actual Syllables"),
	SyllableAlignment("Alignment"),
	Segment("Segment"),
	Notes("Notes");
	
	private String tierName;
	
	private SystemTierType(String tierName) {
		this.tierName = tierName;
	}
	
	public String getTierName() {
		return tierName;
	}
	
	public static boolean isSystemTier(String tierName) {
		return tierFromString(tierName) != null;
	}
	
	public boolean isGroupedTier() {
		if(this == Orthography ||
				this == IPATarget ||
				this == IPAActual ||
				this == TargetSyllables ||
				this == ActualSyllables ||
				this == SyllableAlignment)
			return true;
		else
			return false;
	}
	
	public static SystemTierType tierFromString(String tierName) {
		for(SystemTierType t:values()) {
			if(t.getTierName().equals(tierName))
				return t;
		}
		
		return null;
	}
}

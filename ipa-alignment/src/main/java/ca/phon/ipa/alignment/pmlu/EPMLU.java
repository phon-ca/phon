/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.ipa.alignment.pmlu;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

/**
 * "Extended Phonological Mean Length of Utterance" (Arias & Lleó 2013)
 * calculations for {@link PhoneMap}s
 *
 */
@Extension(PhoneMap.class)
public class EPMLU {
	
	private final PhoneMap phoneMapRef;
	
	public EPMLU(PhoneMap pm) {
		this.phoneMapRef = pm;
	}
	
	public PhoneMap getPhoneMap() {
		return phoneMapRef;
	}

	/**
	 * Calculate actual ePMLU-Features.  This is the number of consonants
	 * times 3 (one point for each Manner, Place, and Voicing)
	 * 
	 * @return target eMPLU-Features
	 */
	public int targetEPMLUFeatures() {
		final PhoneMap pm = getPhoneMap();
		final IPATranscript ipaT = pm.getTargetRep();
		
		int pmlu = 0;
		final PhonexPattern pattern = PhonexPattern.compile("\\c");
		final PhonexMatcher matcher = pattern.matcher(ipaT);
		while(matcher.find()) pmlu += 3;
		
		return pmlu;
	}
	
	/**
	 * Calculate actual ePMLU-Features.  This will be the number of consonants,
	 * with one point each for manner, place, and voicing match.
	 * 
	 * @return actual ePMLU-Features
	 */
	public int actualEPMLUFeatures() {
		final PhoneMap pm = getPhoneMap();
		int pmlu = 0;
		
		for(int i = 0; i < pm.getAlignmentLength(); i++) {
			final IPAElement targetEle = pm.getTopAlignmentElements().get(i);
			final IPAElement actualEle = pm.getBottomAlignmentElements().get(i);
			
			if(targetEle != null) {
				if(targetEle.getFeatureSet().hasFeature("Consonant")) {
					if(actualEle != null && actualEle.getFeatureSet().hasFeature("Consonant")) {
						// check manner
						if(targetEle.getFeatureSet().getManner().equals(actualEle.getFeatureSet().getManner())) 
							++pmlu;
						// check place
						if(targetEle.getFeatureSet().getPlace().equals(actualEle.getFeatureSet().getPlace()))
							++pmlu;
						// check voicing
						if(targetEle.getFeatureSet().getVoicing().equals(actualEle.getFeatureSet().getVoicing()))
							++pmlu;
					}
				}
			}
		}
		
		return pmlu;
	}
	
	/** 
	 * ePWP-Features
	 * 
	 * @return actualEMPLUFeatures()/targetEMPLUFeatures()
	 */
	public float ePWPFeatures() {
		return (targetEPMLUFeatures() > 0 ? (float)actualEPMLUFeatures()/(float)targetEPMLUFeatures() : 0);
	}

	/**
	 * Calculate target ePMLU-Syllables.  This will be the number of syllables times 2, plus
	 * the bonus amount for every closed syllable.
	 * 
	 * @param closedSyllBonus
	 * 
	 * @return target ePMLU-Syllables
	 */
	public float targetEPMLUSyllables(float closedSyllBonus) {
		final PhoneMap pm = getPhoneMap();
		final IPATranscript t = 
				(pm.getTargetRep() != null ? pm.getTargetRep() : new IPATranscript());
		
		float pmlu = 0.0f;
		for(IPATranscript syll:t.syllables()) {
			pmlu += 2.0f;
			if(syll.matches("^\\c.+\\c$")) {
				pmlu += closedSyllBonus;
			}
		}
		return pmlu;
	}
	
	/**
	 * targetEMPLUSyllables with a closedSyllBonus of 1.0f
	 * 
	 * @return targetEMPLUSyllables
	 */
	public float targetEPMLUSyllables() {
		return targetEPMLUSyllables(1.0f);
	}
	
	/**
	 * Calculate actual ePMLU-Syllables.  This will be the number of syllables times 2, plus
	 * the bonus amount for every closed syllable.
	 * 
	 * @param closedSyllBonus
	 * 
	 * @return actual ePMLU-Syllables
	 */
	public float actualEPMLUSyllables(float closedSyllBonus) {
		final PhoneMap pm = getPhoneMap();
		final IPATranscript t = 
				(pm.getActualRep() != null ? pm.getActualRep() : new IPATranscript());
		
		float pmlu = 0.0f;
		for(IPATranscript syll:t.syllables()) {
			pmlu += 2.0f;
			if(syll.matches("^\\c.+\\c$")) {
				pmlu += closedSyllBonus;
			}
		}
		return pmlu;
	}
	
	public float actualEPMLUSyllables() {
		return actualEPMLUSyllables(1.0f);
	}
	
	public float ePWPSyllables(float closedSyllBonus) {
		return ( targetEPMLUSyllables(closedSyllBonus) > 0 ?
					actualEPMLUSyllables(closedSyllBonus)/targetEPMLUSyllables(closedSyllBonus) : 0);
	}
	
	public float ePWPSyllables() {
		return ePWPSyllables(1.0f);
	}
	
	public float targetEPMLU(float closedSyllBonus) {
		return targetEPMLUFeatures() + targetEPMLUSyllables(closedSyllBonus);
	}
	
	public float targetEPMLU() {
		return targetEPMLU(1.0f);
	}
	
	public float actualEPMLU(float closedSyllBonus) {
		return actualEPMLUFeatures() + actualEPMLUSyllables(closedSyllBonus);
	}
	
	public float actualEPMLU() {
		return actualEPMLU(1.0f);
	}
	
	public float ePWP(float closedSyllBonus) {
		return (targetEPMLU(closedSyllBonus) > 0 ? 
					actualEPMLU(closedSyllBonus) / targetEPMLU(closedSyllBonus) : 0);
	}
	
	public float ePWP() {
		return ePWP(1.0f);
	}
	
}

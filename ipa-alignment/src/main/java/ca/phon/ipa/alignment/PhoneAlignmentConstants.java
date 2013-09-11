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

package ca.phon.ipa.alignment;

/**
 * Constants for rewards and penalties during alignment
 * 
 */
public class PhoneAlignmentConstants {
	/**
	 * Reward for aligning spacers with spacers
	 */
	public static final int RSpacerToSpacer = 29;
	
	/**
	 * Reward for aligning primary stress
	 */
	public static final int RPrimaryStressMatch = 72;
	
	/**
	 * Reward for aligning primary stress with secondary stress
	 */
	public static final int RPrimaryToSecondaryStress = -30;
	
	/**
	 * Reward for aligning secondary stress
	 */
	public static final int RSecondaryStressMatch = 0;
	
	/**
	 * Reward for aligning identical phones
	 */
	public static final int RPhoneMatch = 21;
	
	/**
	 * Reward for vowels in stressed syllables (syllabic alignment)
	 */
	public static final int RStressedVowel = 129;
	
	/**
	 * Reward for aligning stressed vowels (segmental alignment)
	 */
	public static final int RStressedVowelOnly = 186;
	
	/**
	 * Reward for phones in the same syllable constituent
	 */
	public static final int RSyllableConstituent = -21;
	
	/**
	 * Reward for phones sharing the same position
	 */
	public static final int RArticulationMatch = 15;
	
	/**
	 * Reward for aligning vowels
	 */
	public static final int RVowel = 59;
	
	/**
	 * Penalty for aligning a spacer with an index
	 */
	public static final int PSpacerToIndel = -75;
	
	/**
	 * Penalty for aligning a spacer with a phone
	 */
	public static final int PSpacerToPhone = -126;
	
	/**
	 * Penalty for indels
	 */
	public static final int PIndel = -10;
	
	/**
	 * Diphthong penalty
	 */
	public static final int PDiphthong = 26;
	
	/**
	 * Penalty for aligning a vowel with a consonant
	 */
	public static final int PVowelToConsonant = 0;
	
	/**
	 * Value by which the cound of common features is multiplies
	 */
	public static final int FeatureMultiplier = 51;
	
	/**
	 * Stress syllable score
	 */
	public static final double StressSyllableScore = 1.0;
}

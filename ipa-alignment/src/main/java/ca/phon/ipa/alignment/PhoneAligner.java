/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.ipa.alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ca.phon.alignment.Aligner;
import ca.phon.alignment.AlignmentMap;
import ca.phon.ipa.AudiblePhoneVisitor;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.syllable.SyllableStress;
import ca.phon.syllable.SyllableVisitor;

/**
 * Aligner for Phone objects with special contants setup for cost
 * susbstitutions.
 * 
 */
public class PhoneAligner implements Aligner<IPAElement> {

	/**
	 * Creates a phone map
	 * 
	 * @param targetRep the target phonetic rep
	 * @param actualRep the actual phonetic rep
	 * @param rewardStress indicates weather the stress reward is used
	 * @param rewardExactMatch
	 * @param rewardStressedVowels
	 * @param rewardPlace
	 * @return Phone[2][] the mapping
	 */
	protected Integer[][] createPhoneMap(
			IPATranscript targetRep, IPATranscript actualRep,
			boolean rewardStress,
			boolean rewardExactMatch,
			boolean rewardStressedVowels,
			boolean rewardPlace) {
		final List<IPATranscript> targetSylls = targetRep.syllables();
		final List<IPATranscript> actualSylls = actualRep.syllables();
		
		final IPATranscript targetPhones = targetRep.removePunctuation(true);
		final IPATranscript actualPhones = actualRep.removePunctuation(true);
		
		int matrix[][];
		int width, height, score;
		Integer[][] retVal = new Integer[2][];
		Stack<Integer> tp, ap;
		
		width = targetPhones.length()+1;
		height = actualPhones.length()+1;
		tp = new Stack<Integer>();
		ap = new Stack<Integer>();
		
		matrix = new int[width][height];
		
		// set the top row and left column to reflect the PIndel costs
		matrix[0][0] = 0;
		
		for(int i = 1; i < width; i++)
			matrix[i][0] = matrix[i-1][0] + this.costSkip(targetPhones.elementAt(i-1));
		
		for(int j = 1; j < height; j++)
			matrix[0][j] = matrix[0][j-1] + this.costSkip(actualPhones.elementAt(j-1));
		
		// fill in the matrix
		for(int i = 1; i < width; i++) {
			for(int j = 1; j < height; j++) {
				int values[] = new int[3];
				
				values[0] = matrix[i-1][j] + this.costSkip(targetPhones.elementAt(i-1));
				values[1] = matrix[i][j-1] + this.costSkip(actualPhones.elementAt(j-1));
				values[2] = matrix[i-1][j-1] + 
					this.costSubstitute(targetRep, i-1,
							actualRep, j-1,
							targetPhones, actualPhones,
							targetSylls, actualSylls,
							rewardStress, rewardExactMatch,
							rewardStressedVowels, rewardPlace);
				
				matrix[i][j] = this.max(values);
			}
		}
		
		score = matrix[width-1][height-1];
		
		retVal = this.retreiveAlignment(
				width-1, height-1, 0, matrix, targetRep, actualRep, 
				targetPhones, actualPhones,
				targetSylls, actualSylls,
				tp, ap, rewardStress, rewardExactMatch, rewardStressedVowels, rewardPlace,score);
		
		
		return retVal;
	}
	
	/**
	 * Creates the syllable alignment
	 * 
	 * @param targetRep
	 * @param actualRep
	 * @param targetPhoneAlignment
	 * @param actualPhoneAlignment
	 */
	protected Integer[][] createSyllableMap(
			IPATranscript targetRep, IPATranscript actualRep,
			Integer[] targetPhoneAlignment, Integer[] actualPhoneAlignment) {
		Integer[][] retVal = new Integer[2][];
		
		final SyllableVisitor syllVisitor = new SyllableVisitor();
		
		targetRep.accept(syllVisitor);
		final List<IPATranscript> targetSylls = syllVisitor.getSyllables();
				
		syllVisitor.reset();
		actualRep.accept(syllVisitor);
		final List<IPATranscript> actualSylls = syllVisitor.getSyllables();
		
		ArrayList<Mapping> mappings = new ArrayList<Mapping>();
		
		int targetIndex = -1;
		int actualIndex = -1;
		
		final AudiblePhoneVisitor soundPhoneVisitor = new AudiblePhoneVisitor();
		targetRep.accept(soundPhoneVisitor);
		final List<IPAElement> targetPhones = soundPhoneVisitor.getPhones();
		
		soundPhoneVisitor.reset();
		actualRep.accept(soundPhoneVisitor);
		final List<IPAElement> actualPhones = soundPhoneVisitor.getPhones();
		
		// add mappings for present alignments
		for(int i = 0; i < targetPhoneAlignment.length; i++) {
			final IPAElement targetPhone = 
				(targetPhoneAlignment[i] < 0 ? null : targetPhones.get(targetPhoneAlignment[i]));
			final IPAElement actualPhone = 
				(actualPhoneAlignment[i] < 0 ? null : actualPhones.get(actualPhoneAlignment[i]));
			targetIndex = getSyllableIndex(targetRep, targetPhone, targetSylls);
			actualIndex = getSyllableIndex(actualRep, actualPhone, actualSylls);
			
			if(targetIndex >= 0 && actualIndex >= 0) {
				Mapping newMap = new Mapping();
				newMap.targetIndex = targetIndex;
				newMap.actualIndex = actualIndex;
				
				if(!mappings.contains(newMap))
					mappings.add(newMap);
			}
		}
		
		// add indels for deleted syllables
		int[] targetSyllableCount = new int[targetSylls.size()];
		int[] actualSyllableCount = new int[actualSylls.size()];
		
		for(Mapping m:mappings) {
			targetSyllableCount[m.targetIndex]++;
			actualSyllableCount[m.actualIndex]++;
		}
		
		for(int i = 0; i < targetSyllableCount.length; i++) {
			if(targetSyllableCount[i] == 0) {
				Mapping newMap = new Mapping();
				newMap.targetIndex = i;
				newMap.actualIndex = -1;
				
				mappings.add(newMap);
			}
		}
		
		for(int i = 0; i < actualSyllableCount.length; i++) {
			if(actualSyllableCount[i] == 0) {
				Mapping newMap = new Mapping();
				newMap.targetIndex = -1;
				newMap.actualIndex = i;
				
				mappings.add(newMap);
			}
		}
		
		// create the syllable arrays
		Integer[] targetSyllableAlignment = new Integer[mappings.size()];
		Integer[] actualSyllableAlignment = new Integer[mappings.size()];
		
		Collections.sort(mappings);
		
		int index = 0;
		for(Mapping m:mappings) {
			if(m.targetIndex >= 0) {
				targetSyllableAlignment[index] = m.targetIndex;
			} else {
				targetSyllableAlignment[index] = AlignmentMap.INDEL_VALUE;
			}
			
			
			if(m.actualIndex >= 0) {
				actualSyllableAlignment[index] = m.actualIndex;
			} else {
				actualSyllableAlignment[index] = AlignmentMap.INDEL_VALUE;
			}
			
			index++;
		}
		
		retVal[0] = targetSyllableAlignment;
		retVal[1] = actualSyllableAlignment;
				
		return retVal;
	}
	
	/**
	 * Return the cost to insert or delete a phone
	 * 
	 * @param p the phone
	 * @return int
	 */
	protected int costSkip(IPAElement p) {
		// check for indel
		if(p == null || !isSpacer(p)) {
			return PhoneAlignmentConstants.PIndel;
		} else {
			return PhoneAlignmentConstants.PSpacerToIndel;
		}
	}
	
	/**
	 * Return the max value
	 * 
	 * @param values
	 * @return int
	 */
	private int max(int[] values) {
		int retVal = 0;
		
		for(int i = 0; i < values.length; i++)
			if(values[i] > retVal)
				retVal = values[i];
		
		return retVal;
	}
	
	/**
	 * Return the similarity between two phones.
	 * 
	 * @param targetPhones
	 * @param targetIndex
	 * @param actualPhones
	 * @param actualIndex
	 * @param rewardStress
	 * @param rewardExactMatch
	 * @param rewardStressedVowels
	 * @param rewardPlace
	 * @return int
	 */
	protected int costSubstitute(
			IPATranscript targetRep, int targetIndex,
			IPATranscript actualRep, int actualIndex,
			IPATranscript targetPhones, IPATranscript actualPhones,
			List<IPATranscript> targetSylls, List<IPATranscript> actualSylls,
			boolean rewardStress, boolean rewardExactMatch,
			boolean rewardStressedVowels, boolean rewardPlace) {
		int count = 0;
		int tally = PhoneAlignmentConstants.FeatureMultiplier;
		
		final IPAElement a = (targetIndex < targetPhones.length() ? targetPhones.elementAt(targetIndex) : null);
		final IPAElement b = (actualIndex < actualPhones.length() ? actualPhones.elementAt(actualIndex) : null);
				
		// check for spacers
		if(isSpacer(a)) {
			if(isSpacer(b)) {
				if(PhoneAlignmentConstants.RSpacerToSpacer != 0) {
					return PhoneAlignmentConstants.RSpacerToSpacer;
				}
			} else {
				if(PhoneAlignmentConstants.PSpacerToPhone != 0) {
					return PhoneAlignmentConstants.PSpacerToPhone;
				}
			}
		}
		
		if(isSpacer(b)) {
			if(isSpacer(a)) {
				if(PhoneAlignmentConstants.RSpacerToSpacer != 0) {
					return PhoneAlignmentConstants.RSpacerToSpacer;
				}
			} else {
				if(PhoneAlignmentConstants.PSpacerToPhone != 0) {
					return PhoneAlignmentConstants.PSpacerToPhone;
				}
			}
		}
		
		// look for common features
		FeatureSet interFs = FeatureSet.intersect(
				a.getFeatureSet(), b.getFeatureSet());
		count = interFs.size();
		
		if(
				(a.getFeatureSet().hasFeature("Vowel") && 
						b.getFeatureSet().hasFeature("Consonant"))
				||
				(a.getFeatureSet().hasFeature("Consonant") && 
						b.getFeatureSet().hasFeature("Vowel"))
		) {
			if(PhoneAlignmentConstants.PVowelToConsonant != 0)
				return PhoneAlignmentConstants.PVowelToConsonant;
		}
		
		// reward for feature being in the same constituent
		if(inSameSyllableConstituent(a,b)) {
			tally += PhoneAlignmentConstants.RSyllableConstituent;
		}
		
		if(isHelperVowel(a)
				&& isVowel(b)
				&& isInDiphthong(targetRep, targetIndex)) {
			if(PhoneAlignmentConstants.PDiphthong != 0) {
				return PhoneAlignmentConstants.PDiphthong;
			}
		} else if(isVowel(a)
				&& isHelperVowel(b)
				&& isInDiphthong(actualRep, actualIndex)) {
			if(PhoneAlignmentConstants.PDiphthong != 0) {
				return PhoneAlignmentConstants.PDiphthong;
			}
		}
		
		SyllableStress targetStress = getSyllableStress(targetRep, a, targetSylls);
		SyllableStress actualStress = getSyllableStress(actualRep, b, actualSylls);
		
		// if we are rewarding stress ...
		if(rewardStress) {
			if(targetStress == SyllableStress.PrimaryStress && actualStress == SyllableStress.PrimaryStress) {
				// match for primary stress
				tally += (int)(
						PhoneAlignmentConstants.RPrimaryStressMatch * PhoneAlignmentConstants.StressSyllableScore);
				
				if(isVowel(a) && isVowel(b)) {
					tally += (int)(
						PhoneAlignmentConstants.RStressedVowel * PhoneAlignmentConstants.StressSyllableScore);
				}
			} else if(
					(targetStress == SyllableStress.PrimaryStress 
							&& actualStress == SyllableStress.SecondaryStress) |
					(targetStress == SyllableStress.SecondaryStress
							&& actualStress == SyllableStress.PrimaryStress)) {
				// match for different stress
				tally += PhoneAlignmentConstants.RPrimaryToSecondaryStress;
			} else if(targetStress == SyllableStress.SecondaryStress && actualStress == SyllableStress.SecondaryStress) {
				// matched secondary stress
				tally += PhoneAlignmentConstants.RSecondaryStressMatch;
			}
		}
		
		// check if we are rewarding only stressed vowels
		if(rewardStressedVowels) {
			if(isVowel(a) && isVowel(b)
					&& targetStress == SyllableStress.PrimaryStress
					&& actualStress == SyllableStress.PrimaryStress)
				tally += PhoneAlignmentConstants.RStressedVowelOnly;
		}
		
		if(isVowel(a) && isVowel(b))
			tally += PhoneAlignmentConstants.RVowel;
		
		// check for same articulation
		if(rewardPlace) {
			FeatureSet intersection = FeatureSet.intersect(a.getFeatureSet(), b.getFeatureSet());
			if(intersection.hasFeature("Coronal")
					|| intersection.hasFeature("Labial")
					|| intersection.hasFeature("Velar"))
				tally += PhoneAlignmentConstants.RArticulationMatch;
		}
		
		
		// return tally multiplied by the number of common features
		return count * tally;
	}
	
	/**
	 * Get the alignment from the completed matrix
	 * 
	 * @param i the width of the matrix
	 * @param j the height of the matrix
	 * @param tally the total score accumulated in at each setp of the recursion
	 * @param Matrix the completed dynamic algorithm
	 * @param targetRep 
	 * @param actualRep
	 * @param tp the stack used to store the target alignment
	 * @param ap the stack used to store the actual alignment
	 * @param rewardStress
	 * @param rewardExactMatch
	 * @param rewardStressedVowels
	 * @param rewardPlace
	 * @param score
	 */
	protected Integer[][] retreiveAlignment(
			int i, int j, int tally, int[][] matrix,
			IPATranscript targetRep, IPATranscript actualRep,
			IPATranscript targetPhones, IPATranscript actualPhones,
			List<IPATranscript> targetSylls, List<IPATranscript> actualSylls,
			Stack<Integer> tp, Stack<Integer> ap,
			boolean rewardStress, boolean rewardExactMatch, 
			boolean rewardStressedVowels, boolean rewardPlace,
			int score) {
		
		
		// the base case for our recursion, we are looking at a 0x0 matrix
		if(i == 0 && j == 0) {
			Integer[][] toReturn = new Integer[2][];
			
			Integer[] t = new Integer[tp.size()];
			Integer[] a = new Integer[ap.size()];
			
			for(int k = 0; !tp.empty(); k++) {
				t[k] = tp.pop();
			}
			
			for(int l = 0; !ap.empty(); l++) {
				a[l] = ap.pop();
			}
			
			toReturn[0] = t;
			toReturn[1] = a;
			
			return toReturn;
		}
		
		// check if the maximum value is for a match
		if(i > 0 && j > 0) {
			int subVal = this.costSubstitute(
					targetRep, i-1, actualRep, j-1,
					targetPhones, actualPhones,
					targetSylls, actualSylls,
					rewardStress, rewardExactMatch, rewardStressedVowels,
					rewardPlace);
			
			int chkVal = matrix[i-1][j-1] + subVal + tally;
			
			if(chkVal >= score) {
				tp.push(i-1);
				ap.push(j-1);
				
				int newTally = tally + subVal;
				
				return this.retreiveAlignment(
						i-1, j-1, newTally,
						matrix, targetRep, actualRep, 
						targetPhones, actualPhones,
						targetSylls, actualSylls,
						tp, ap,
						rewardStress, rewardExactMatch,
						rewardStressedVowels, rewardPlace, score);
			}
		}
		
		// check if max value came from skipping target[i]
		if(j > 0) {
			int chkVal = matrix[i][j-1] + this.costSkip(actualPhones.elementAt(j-1)) + tally;
			
			if(chkVal >= score) {
//				Phone stackPhone = null; // make an indel
				
				if(isSpacer(actualPhones.elementAt(j-1))) {
					tp.push(AlignmentMap.SPACER_VALUE); // put the new phone
				} else {
					tp.push(AlignmentMap.INDEL_VALUE);
				}
				ap.push(j-1); // with actual
				
				int newTally = tally + this.costSkip(actualPhones.elementAt(j-1));
				
				return this.retreiveAlignment(
						i, j-1, newTally,
						matrix, targetRep, actualRep, 
						targetPhones, actualPhones,
						targetSylls, actualSylls,
						tp, ap,
						rewardStress, rewardExactMatch,
						rewardStressedVowels, rewardPlace, score);
			}
		}
		
		// check if max value came from skipping actual[j]
		if(i > 0) {
//			Phone stackPhone = null; // make an indel
			
			if(isSpacer(targetPhones.elementAt(i-1))) {
				ap.push(AlignmentMap.SPACER_VALUE);
			} else {
				ap.push(AlignmentMap.INDEL_VALUE);
			}
			
			tp.push(i-1);
//			ap.push(stackPhone);
			
			int newTally = tally + this.costSkip(targetPhones.elementAt(i-1));
			
			return this.retreiveAlignment(
					i-1, j, newTally,
					matrix, targetRep, actualRep, 
					targetPhones, actualPhones,
					targetSylls, actualSylls,
					tp, ap,
					rewardStress, rewardExactMatch,
					rewardStressedVowels, rewardPlace, score);
		} else {
			// special case - i = 0, j = 1
			// (_)XXXX
			// (X)__XX
			// We need to add an indel on the target side to align with the first
			// actual phone
			if(i == 0 && j > 0) {
				tp.push(AlignmentMap.INDEL_VALUE);
				ap.push(j-1);
				
				int newTally = tally + this.costSkip(targetPhones.elementAt(i));
				
				return this.retreiveAlignment(i, j-1, newTally, 
						matrix, targetRep, actualRep,
						targetPhones, actualPhones,
						targetSylls, actualSylls,
						tp, ap, 
						rewardStress, rewardExactMatch, 
						rewardStressedVowels, rewardPlace, score);
			} else {
				// return DEFAULT alignment
				// NOTE: should never get here
				//PhonLogger.warning("[Aligner] Could not determine an appropriate alignment, returning default.");
				
				
				int maxLen = Math.max(targetPhones.length(), actualPhones.length());
				Integer[][] toReturn = new Integer[2][];
				toReturn[0] = new Integer[maxLen];
				int tIndex = 0;
				for( ; tIndex < targetPhones.length(); tIndex++)
					toReturn[0][tIndex] = tIndex;
				for(int pIndex = tIndex; pIndex < maxLen; pIndex++)
					toReturn[0][pIndex] = AlignmentMap.INDEL_VALUE;
				
				toReturn[1] = new Integer[maxLen];
				int aIndex = 0;
				for( ; aIndex < actualPhones.length(); aIndex++)
					toReturn[1][aIndex] = aIndex;
				for(int pIndex = aIndex; pIndex < maxLen; pIndex++)
					toReturn[1][pIndex] = AlignmentMap.INDEL_VALUE;
				
				return toReturn;
			}
		}
	}
	
	/* Helper methods */
	/**
	 * Are the two phones in the same syllable constituent type?
	 * 
	 * @param a
	 * @param b
	 * @return boolean
	 */
	private boolean inSameSyllableConstituent(IPAElement targetPhone, IPAElement actualPhone) {
		return targetPhone.getScType() == actualPhone.getScType();
	}
	
	/**
	 * Is the given phone a member of a diphthong?
	 * @param phoReb
	 * @param phoneIndex
	 * @return boolean
	 */
	private boolean isInDiphthong(IPATranscript phoRep, int phoneIndex) {
		boolean me = false;
		boolean inFront = false;
		boolean behind = false;
		final AudiblePhoneVisitor soundPhoneVisitor = new AudiblePhoneVisitor();
		phoRep.accept(soundPhoneVisitor);
		IPAElement[] soundPhones = soundPhoneVisitor.getPhones().toArray(new IPAElement[0]);
		
		// get the phones
		IPAElement p = null;
		if(phoneIndex < soundPhones.length)
			p = soundPhones[phoneIndex];
		else return false;
		
		IPAElement i = null;
		if(phoneIndex-1 > 0)
			i = soundPhones[phoneIndex-1];
		
		IPAElement b = null;
		if(phoneIndex+1 < soundPhones.length)
			b = soundPhones[phoneIndex+1];
		
		if(isCoreVowel(p) || isHelperVowel(p))
			me = true;
		
		if(isCoreVowel(i) || isHelperVowel(i))
			inFront = true;
		
		if(isCoreVowel(b) || isHelperVowel(b))
			behind = true;
		
		return  (
				(me && inFront) || (me && behind) );
	}
	
	/**
	 * Get the syllable index of a phone
	 * 
	 * @param phoRep
	 * @param p
	 * @return int
	 */
	private int getSyllableIndex(IPATranscript phoRep, IPAElement p, List<IPATranscript> syllList) {
		int retVal = -1;
		
		if(p == null || phoRep == null || syllList == null)
			return retVal;
		for(int i = 0; i < syllList.size(); i++) {
			IPATranscript syll = syllList.get(i);
			if(syll.indexOf(p) >= 0) {
				retVal = syll.indexOf(p);
				break;
			}
		}
		
		return retVal;
	}

	/**
	 * Tells the stress of the syllable owning the phone.
	 * @param phoRep
	 * @param p
	 * @return SyllableStress
	 */
	private SyllableStress getSyllableStress(IPATranscript phoRep, IPAElement p, List<IPATranscript> syllList) {
		int syllIndex = getSyllableIndex(phoRep, p, syllList);
		
		if(syllIndex == -1 || syllIndex >= syllList.size())
			return SyllableStress.NoStress;
		
		IPATranscript syll = syllList.get(syllIndex);
		if(syll.length() == 0) return SyllableStress.NoStress;
		
		if(syll.elementAt(0).getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
			if(syll.elementAt(0).toString().equals(SyllableStress.PrimaryStress.getIpa()+""))
				return SyllableStress.PrimaryStress;
			else if(syll.elementAt(0).toString().equals(SyllableStress.SecondaryStress.getIpa() + ""))
				return SyllableStress.SecondaryStress;
		}
		
		return SyllableStress.NoStress;
	}
	
	/**
	 * For alignment purposes, define a 'spacer' as a phone
	 * with value " "
	 */
	private boolean isSpacer(IPAElement p) {
		if(isIndel(p)) return false;
		return p.toString().equals(" ");
	}
	
	private boolean isIndel(IPAElement p) {
		return p == null;
	}
	
	private boolean isVowel(IPAElement p) {
		if(isIndel(p)) return false;
		return p.getFeatureSet().hasFeature("Vowel");
	}
	
	private boolean isConsonant(IPAElement p) {
		if(isIndel(p)) return false;
		return p.getFeatureSet().hasFeature("Consonant");
	}
	
	private boolean isGlide(IPAElement p) {
		if(isIndel(p)) return false;
		return p.getFeatureSet().hasFeature("Glide");
	}
	
	private boolean isHighLaxVowel(IPAElement p) {
		if(isIndel(p)) return false;
		return (
				isVowel(p)
				&& p.getFeatureSet().hasFeature("High")
				&& !p.getFeatureSet().hasFeature("ATR")
				);
	}
	
	private boolean isTenseVowel(IPAElement p) {
		if(isIndel(p)) return false;
		return (
				isVowel(p)
				&& !isHighLaxVowel(p));
	}
	
	private boolean isLowVowel(IPAElement p) {
		if(isIndel(p)) return false;
		
		return (
				isVowel(p)
				&& p.getFeatureSet().hasFeature("Low"));
	}
	
	private boolean isCoreVowel(IPAElement p) {
		if(isIndel(p)) return false;
		
		if(isVowel(p)) {
			if(p.getFeatureSet().hasFeature("High")) {
				if(p.getFeatureSet().hasFeature("ATR"))
					return true;
				else
					return false;
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isHelperVowel(IPAElement p) {
		if(isIndel(p)) return false;
		
		return (
				isGlide(p) || isHighLaxVowel(p));
	}
	
	@Override
	public AlignmentMap<IPAElement> calculateAlignment(IPAElement[] top, IPAElement[] bottom) {
		return calculatePhoneMap(new IPATranscript(top), new IPATranscript(bottom));
	}
	
	public PhoneMap calculatePhoneMap(IPATranscript target, IPATranscript actual) {
		final Integer[][] alignment = createPhoneMap(target, actual, true, true, true, true);
				
		final PhoneMap pm = new PhoneMap(target, actual);
		pm.setTopAlignment(alignment[0]);
		pm.setBottomAlignment(alignment[1]);
		
		return pm;
	}

	@Override
	public AlignmentMap<IPAElement> getAlignmentMap() {
		// TODO Auto-generated method stub
		return null;
	}

	/* Class for internal workings */
	private class Mapping implements Comparable {
		int targetIndex;
		int actualIndex;
		
		@Override
		public boolean equals(Object b) {
			Mapping bObj = (Mapping)b;
			
			return (bObj.targetIndex == targetIndex) && (bObj.actualIndex == actualIndex);
		}

		@Override
		public int compareTo(Object arg0) {
			Mapping m = (Mapping)arg0;
			
			if(targetIndex == -1 || m.targetIndex == -1) {
				if(actualIndex > m.actualIndex)
					return 1;
				else if(actualIndex == m.actualIndex)
					return 0;
				else
					return -1;
			}			
			
			if(targetIndex == m.targetIndex) {
				if(actualIndex > m.actualIndex)
					return 1;
				else if(actualIndex == m.actualIndex)
					return 0;
				else
					return -1;
			}
			
			if(targetIndex > m.targetIndex)
				return 1;
			else if(targetIndex < m.targetIndex)
				return -1;
			else
				return 0;
		}

	}
	
}

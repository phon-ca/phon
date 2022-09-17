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

import ca.phon.alignment.*;
import ca.phon.ipa.*;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.syllable.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IndelAligner} for {@link IPATranscript}s.
 * 
 */
public class PhoneAligner extends IndelAligner<IPAElement> {

	private IPATranscript targetRep;

	private IPATranscript actualRep;

	private List<IPATranscript> targetSylls;

	private List<IPATranscript> actualSylls;

	private boolean hasStressedSyllables = false;

	public PhoneAligner() {
	}

	@Override
	protected int costSubstitute(IPAElement ele1, IPAElement ele2) {
		int tally = 0;

		if( (ele1.getFeatureSet().hasFeature("Consonant")
				&& ele2.getFeatureSet().hasFeature("Consonant")) ) {
			tally += 2;
		} else if( (ele1.getFeatureSet().hasFeature("Vowel")
				&& ele2.getFeatureSet().hasFeature("Vowel")) ) {
			tally += 2;
		} else {
			final IPAElement vowel = (ele1.getFeatureSet().hasFeature("Vowel") ? ele1 : ele2);
			final IPAElement notvowel = (vowel == ele1 ? ele2 : ele1);

			if(notvowel.getFeatureSet().hasFeature("syllabic")) {
				return 2;
			} else {
				// align if toString() matches
				if(ele1.toString().equals(ele2.toString())) {
					return 4;
				} else {
					return -1;
				}
			}
		}

		final SyllableConstituentType t1 = ele1.getScType();
		final SyllableConstituentType t2 = ele2.getScType();
		if(t1 == t2) {
			tally += 1;
		} else {
			tally -= 1;
		}

		if(hasStressedSyllables) {
			final SyllableStress s1 = stressForElement(ele1);
			final SyllableStress s2 = stressForElement(ele2);
			if(s1 == s2) {
				tally += 2;
			}
		}

		final PhoneticProfile p1 = new PhoneticProfile(ele1);
		final PhoneticProfile p2 = new PhoneticProfile(ele2);
		int featureTally = 0;
		if(p1.isConsonant()) {
			featureTally += checkDimension(p1, p2, PhoneDimension.PLACE);
			featureTally += checkDimension(p1, p2, PhoneDimension.MANNER);
			featureTally += checkDimension(p1, p2, PhoneDimension.VOICING);
		} else {
			featureTally += checkDimension(p1, p2, PhoneDimension.HEIGHT);
			featureTally += checkDimension(p1, p2, PhoneDimension.BACKNESS);
			featureTally += checkDimension(p1, p2, PhoneDimension.TENSENESS);
			featureTally += checkDimension(p1, p2, PhoneDimension.ROUNDING);
		}
		// if no features match, only subtract 1
		tally += Math.max(-1, featureTally);

		String ele1Base = ((Phone)ele1).getBase();
		String ele2Base = ((Phone)ele2).getBase();
		
		// strong lock if base of phones match
		if(ele1Base.matches(ele2Base))
			tally += 3;
		
		// add extra if one of the bases contains the other
		else if(ele1Base.contains(ele2Base) || ele2Base.contains(ele1Base)) 
			tally += 1;

		return tally;
	}

	private int checkDimension(PhoneticProfile p1, PhoneticProfile p2, PhoneDimension dimension) {
		int retVal = 0;

		final FeatureSet fs1 = FeatureSet.intersect(p1.get(dimension), dimension.getFeatures());
		final FeatureSet fs2 = FeatureSet.intersect(p2.get(dimension), dimension.getFeatures());

		final FeatureSet t = FeatureSet.intersect(fs1, fs2);

		// return if any terminal features intersect
		if(fs1.intersects(fs2))
			retVal = t.size();
		else
			retVal = -1;

		return retVal;
	}

	@Override
	protected int costSkip(IPAElement ele) {
		return 0;
	}

	public List<IPATranscript> getTargetSyllables() {
		return targetSylls;
	}

	public void setTargetSyllables(List<IPATranscript> targetSylls) {
		this.targetSylls = targetSylls;
	}

	public List<IPATranscript> getActualSyllables() {
		return actualSylls;
	}

	public void setActualSyllables(List<IPATranscript> actualSylls) {
		this.actualSylls = actualSylls;
	}

	public IPATranscript getTargetRep() {
		return targetRep;
	}

	public void setTargetRep(IPATranscript targetRep) {
		this.targetRep = targetRep;
	}

	public IPATranscript getActualRep() {
		return actualRep;
	}

	public void setActualRep(IPATranscript actualRep) {
		this.actualRep = actualRep;
	}

	private IPATranscript syllableContainingElement(IPAElement ele) {
		for(IPATranscript syll:getTargetSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}

		for(IPATranscript syll:getActualSyllables()) {
			if(syll.indexOf(ele) >= 0)
				return syll;
		}

		return null;
	}

	private SyllableStress stressForElement(IPAElement ele) {
		final IPATranscript syll = syllableContainingElement(ele);
		return (syll != null ? syll.getExtension(SyllableStress.class) : null);
	}

	/**
	 * Calculate phone alignment.
	 *
	 * Method keep for API compatibility with older plug-ins/scripts.
	 *
	 * @deprecated
	 * @param ipaTarget
	 * @param ipaActual
	 * @return
	 */
	public PhoneMap calculatePhoneMap(IPATranscript ipaTarget, IPATranscript ipaActual) {
		return calculatePhoneAlignment(ipaTarget, ipaActual);
	}

	/**
	 * Calculate phone alignment
	 *
	 * @param ipaTarget
	 * @param ipaActual
	 * @return
	 */
	public PhoneMap calculatePhoneAlignment(IPATranscript ipaTarget, IPATranscript ipaActual) {
		// create word pairs
		int targetEleOffset = 0;
		int actualEleOffset = 0;
		final List<IPATranscript> targetWords = ipaTarget.words();
		final List<IPATranscript> actualWords = ipaActual.words();
		
		int maxWords = Math.max(targetWords.size(), actualWords.size());
		final List<PhoneMap> alignmentMaps = new ArrayList<>();
		for(int i = 0 ; i < maxWords; i++) {
			final IPATranscript targetWord = (i < targetWords.size() ? targetWords.get(i) : new IPATranscript());
			final IPATranscript actualWord = (i < actualWords.size() ? actualWords.get(i) : new IPATranscript());
			
			setTargetRep(targetWord);
			setActualRep(actualWord);

			setTargetSyllables(targetWord.syllables());
			setActualSyllables(actualWord.syllables());

			for(IPATranscript syll:getTargetSyllables()) {
				final SyllableStress stress = syll.getExtension(SyllableStress.class);
				if(stress == SyllableStress.PrimaryStress || stress == SyllableStress.SecondaryStress) {
					hasStressedSyllables = true;
					break;
				}
			}

			final IPATranscript targetPhones = targetWord.audiblePhones();
			final IPATranscript actualPhones = actualWord.audiblePhones();

			final IPAElement targetEles[] = new IPAElement[targetPhones.length()];
			for(int j = 0; j < targetPhones.length(); j++) targetEles[j] = targetPhones.elementAt(j);

			final IPAElement actualEles[] = new IPAElement[actualPhones.length()];
			for(int j = 0; j < actualPhones.length(); j++) actualEles[j] = actualPhones.elementAt(j);

			final AlignmentMap<IPAElement> alignment = calculateAlignment(targetEles, actualEles);
			
			// fix indices
			Integer[] topAlignment = alignment.getTopAlignment();
			for(int j = 0; j < topAlignment.length; j++) {
				topAlignment[j] = (topAlignment[j] != null && topAlignment[j] >= 0 ? topAlignment[j]+targetEleOffset : topAlignment[j]);
			}
			Integer[] bottomAlignment = alignment.getBottomAlignment();
			for(int j = 0; j < bottomAlignment.length; j++) {
				bottomAlignment[j] = (bottomAlignment[j] != null && bottomAlignment[j] >= 0 ? bottomAlignment[j]+actualEleOffset : bottomAlignment[j]);
			}

			final PhoneMap retVal = new PhoneMap(ipaTarget, ipaActual);
			retVal.setTopElements(targetEles);
			retVal.setBottomElements(actualEles);
			retVal.setTopAlignment(topAlignment);
			retVal.setBottomAlignment(bottomAlignment);
			alignmentMaps.add(retVal);
			
			targetEleOffset += targetWord.audiblePhones().length();
			actualEleOffset += actualWord.audiblePhones().length();
		}
		
		final IPATranscript targetPhones = ipaTarget.audiblePhones();
		final IPATranscript actualPhones = ipaActual.audiblePhones();
		
		final IPAElement targetEles[] = new IPAElement[targetPhones.length()];
		for(int j = 0; j < targetPhones.length(); j++) targetEles[j] = targetPhones.elementAt(j);
		
		final IPAElement actualEles[] = new IPAElement[actualPhones.length()];
		for(int j = 0; j < actualPhones.length(); j++) actualEles[j] = actualPhones.elementAt(j);
		
		// add all alignments together
		int alignmentLength = alignmentMaps.stream().map( (pm) -> pm.getAlignmentLength() ).collect( Collectors.summingInt(Integer::intValue) );
		Integer topAlignment[] = new Integer[alignmentLength];
		Integer bottomAlignment[] = new Integer[alignmentLength];
		
		int alignIdx = 0;
		for(int i = 0; i < alignmentMaps.size(); i++) {
			final PhoneMap pm = alignmentMaps.get(i);
		
			Integer[] tAlign = pm.getTopAlignment();
			Integer[] aAlign = pm.getBottomAlignment();
			
			// check for special case involving alignment with '*'
			if(     pm.getAlignmentLength() >= 2
					&&
					// initial indel on top for first position
					(tAlign[0] < 0 && tAlign[1] >= 0 && targetEles[tAlign[1]].toString().equals("*"))
			  )
			{
				// swap first and second alignment values
				int tempTAlign = tAlign[0];
				tAlign[0] = tAlign[1];
				tAlign[1] = tempTAlign;
				
				int tempAAlign = aAlign[0];
				aAlign[0] = aAlign[1];
				aAlign[1] = tempAAlign;
			}
			
			for(int j = 0; j < pm.getAlignmentLength(); j++) {
				topAlignment[alignIdx] = tAlign[j];
				bottomAlignment[alignIdx] = aAlign[j];
				alignIdx++;
			}
		}
		
		final PhoneMap retVal = new PhoneMap(ipaTarget, ipaActual);
		retVal.setTopElements(targetEles);
		retVal.setBottomElements(actualEles);
		retVal.setTopAlignment(topAlignment);
		retVal.setBottomAlignment(bottomAlignment);
		
		return retVal;
	}
	
}

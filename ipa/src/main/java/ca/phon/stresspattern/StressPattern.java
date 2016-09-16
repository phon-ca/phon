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
package ca.phon.stresspattern;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.phon.fsa.FSAState;
import ca.phon.fsa.SimpleFSA;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.stresspattern.fsa.StressPatternCompiler;
import ca.phon.syllable.SyllableStress;
import ca.phon.util.Range;

public class StressPattern {
	
	/** The fsa */
	private final SimpleFSA<StressMatcherType> fsa;
	
	/** The matcher string */
	private final String matcherString;
	
	/**
	 * Compile a string into a StressPattern.
	 * 
	 * @param matcher
	 * @return StressPattern
	 * @throws ParserException
	 */
	public static StressPattern compile(String matcherString) 
		throws ParseException {
		SimpleFSA<StressMatcherType> fsa = (new StressPatternCompiler()).compile(matcherString);
		return new StressPattern(fsa, matcherString);
	}
	
	/**
	 * Hidden constructor.
	 */
	protected StressPattern(SimpleFSA<StressMatcherType> fsa, String matcherString) {
		super();
		this.fsa = fsa;
		this.matcherString = matcherString;
	}
	
	public boolean matches(List<StressMatcherType> stTypes) {
		FSAState<StressMatcherType> lastState = fsa.runWithTape(stTypes.toArray(new StressMatcherType[0]));
		if(lastState.getRunningState() == FSAState.RunningState.EndOfInput &&
				fsa.isFinalState(lastState.getCurrentState())) {
					return true;
		}
		return false;
	}
	
	public boolean findWithin(List<StressMatcherType> stTypes) {
		return findRanges(stTypes).size() != 0;
	}
	
	public HashMap<String, Integer> count(List<StressMatcherType> stTypes) {
		List<Range> matchedRanges = findRanges(stTypes);
		
		HashMap<String, Integer> retVal = new HashMap<String, Integer>();
		
		for(Range matchedRange:matchedRanges) {
			String currentString = "";
			for(int stIndex:matchedRange) {
				currentString += stTypes.get(stIndex).getImage();
			}
			
			Integer val = retVal.get(currentString);
			if(val == null) val = 0;
			val++;
			retVal.put(currentString, val);
		}
		
		return retVal;
	}
	
	public List<Range> findRanges(List<StressMatcherType> stTypes) {
		List<Range> retVal = new ArrayList<Range>();
		
		if(matcherString.startsWith("#")) {
			FSAState<StressMatcherType> lastState = 
				fsa.runWithTape(stTypes.toArray(new StressMatcherType[0]));
			if(fsa.isFinalState(lastState.getCurrentState())) {
				retVal.add(new Range(0, lastState.getTapeIndex(), true));
			}
		} else {
			int currentStart = 0;
			while(currentStart < stTypes.size()) {
				List<StressMatcherType> subList = stTypes.subList(currentStart, stTypes.size());
				FSAState<StressMatcherType> lastState = 
					fsa.runWithTape(subList.toArray(new StressMatcherType[0]));
				if(lastState.getCurrentState() != null &&
						fsa.isFinalState(lastState.getCurrentState())) {
					Range matchedRange = 
						new Range(currentStart, currentStart+lastState.getTapeIndex(), true);
					retVal.add(matchedRange);
				}
				currentStart++;
			}
		}
		
		return Range.reduceRanges(retVal);
	}
	
	/**
	 * Returns the stress pattern for the
	 * given phones.
	 * 
	 * @param phones
	 * @return String
	 */
	public static String getStressPattern(List<IPAElement> allPhones) {
		String wordShape = new String();
		
		final IPATranscript ipa = new IPATranscript(allPhones);
		for(IPATranscript word:ipa.words()) {
			for(IPATranscript syll:word.syllables()) {
				final SyllableStress s = 
						(syll.getExtension(SyllableStress.class) != null ?
							syll.getExtension(SyllableStress.class) : SyllableStress.NoStress);
				if(s == SyllableStress.PrimaryStress) {
					wordShape += "1";
				} else if(s == SyllableStress.SecondaryStress) {
					wordShape += "2";
				} else if(s == SyllableStress.NoStress) {
					wordShape += "U";
				}
			}
			
			wordShape += " ";
		}
		
		return wordShape.trim();
	}
	
	/**
	 * Convert a range for a stress pattern
	 * match into a phone range.
	 * 
	 * @param phones
	 * @param stressPattern
	 * @param range
	 * @return the converted range
	 */
	public static Range convertSPRToPR(
			List<IPAElement> phones, String stressPattern, Range range) {
		final IPATranscript ipa = new IPATranscript(phones);
		// get the syllabification
		List<IPATranscript> sylls = ipa.syllables();
		
		List<IPATranscript> matchedSylls = new ArrayList<IPATranscript>();
		
		int spIndex = 0;
		int realSyllIndex = 0;
		for(char spType:stressPattern.toCharArray()) {
			if(spType != ' ' && range.contains(spIndex)) {
				matchedSylls.add(sylls.get(realSyllIndex));
			}
			
			spIndex++;
			if(spType != ' ')
				realSyllIndex++;
		}
		
		int firstPhoneIndex = 0;
		int lastPhoneIndex = 0;
		if(matchedSylls.size() > 0) {
			IPATranscript firstSyll = matchedSylls.get(0);
			IPAElement[] firstPhones = firstSyll.toList().toArray(new IPAElement[0]);
			IPATranscript lastSyll = matchedSylls.get(matchedSylls.size()-1);
			IPAElement[] lastPhones = lastSyll.toList().toArray(new IPAElement[0]);
			
			firstPhoneIndex = ipa.indexOf(firstPhones[0]);
			lastPhoneIndex = ipa.indexOf(lastPhones[lastPhones.length-1])+1;
		}
		
		return new Range(firstPhoneIndex, lastPhoneIndex, true);
	}
}

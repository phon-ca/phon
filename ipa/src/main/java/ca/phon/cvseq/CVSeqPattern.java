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
package ca.phon.cvseq;

import ca.phon.cvseq.fsa.CVSeqCompiler;
import ca.phon.fsa.*;
import ca.phon.ipa.IPAElement;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;

import java.text.ParseException;
import java.util.*;

/**
 *
 */
public class CVSeqPattern {
	
	/** The fsa */
	private SimpleFSA<CVSeqType> fsa;
	
	/** The matcher string */
	private String matcherString;
	
	public static CVSeqPattern compile(String matcherString)
		throws ParseException {
		CVSeqCompiler compiler = new CVSeqCompiler();
		return new CVSeqPattern(compiler.compile(matcherString), matcherString);
	}
	
	protected CVSeqPattern(SimpleFSA<CVSeqType> fsa, String matcherString) {
		super();
		
		this.fsa = fsa;
		this.matcherString = matcherString;
	}
	
	public boolean matches(List<CVSeqType> stTypes) {
		FSAState<CVSeqType> lastState = fsa.runWithTape(stTypes.toArray(new CVSeqType[0]));
		if(lastState.getRunningState() == FSAState.RunningState.EndOfInput &&
				fsa.isFinalState(lastState.getCurrentState())) {
					return true;
		}
		return false;
	}

	public boolean findWithin(List<CVSeqType> stTypes) {
		return findRanges(stTypes).size() != 0;
	}
	
	public HashMap<String, Integer> count(List<CVSeqType> stTypes) {
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
	
	public List<Range> findRanges(List<CVSeqType> stTypes) {
		List<Range> retVal = new ArrayList<Range>();
		
		if(matcherString.startsWith("#")) {
			FSAState<CVSeqType> lastState = 
				fsa.runWithTape(stTypes.toArray(new CVSeqType[0]));
			if(fsa.isFinalState(lastState.getCurrentState())) {
				retVal.add(new Range(0, lastState.getTapeIndex(), true));
			}
		} else {
			int currentStart = 0;
			while(currentStart < stTypes.size()) {
				List<CVSeqType> subList = stTypes.subList(currentStart, stTypes.size());
				FSAState<CVSeqType> lastState = 
					fsa.runWithTape(subList.toArray(new CVSeqType[0]));
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
	
	public static String getCVSeq(List<IPAElement> phones) {
		String retVal = "";
		
		for(IPAElement p:phones) {
			if(p.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER
					|| p.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER)
				continue;
			else if(p.getScType() == SyllableConstituentType.WORDBOUNDARYMARKER) {
				retVal += " ";
				continue;
			}
			
			if(p.getFeatureSet().hasFeature("Consonant")) {
				if(p.getFeatureSet().hasFeature("Glide")) {
					retVal += "G";
				} else {
					retVal += "C";
				}
			} else if(p.getFeatureSet().hasFeature("Vowel")) {
				retVal += "V";
			}
		}
		
		return retVal;
	}
	
	public static Range convertCVRangeToPhoneRange(List<IPAElement> phones, Range cvRange) {
		int startPhone = -1;
		int endPhone = -1;
		
		int cvIndex = 0;
		int pIndex = 0;
		while(pIndex < phones.size()) {
			IPAElement p = phones.get(pIndex);
			if(p.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER
					|| p.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
				// don't increment cvIndex and continue
				pIndex++;
				continue;
			}
			
			if(cvRange.contains(cvIndex)) {
				if(startPhone < 0)
					startPhone = pIndex;
				endPhone = pIndex;
			}
			
			cvIndex++;
			pIndex++;
		}
		
		return new Range(startPhone, endPhone+1, true);
	}
}

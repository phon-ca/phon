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
package ca.phon.cvseq;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.phon.cvseq.fsa.CVSeqCompiler;
import ca.phon.fsa.FSAState;
import ca.phon.fsa.SimpleFSA;
import ca.phon.ipa.IPAElement;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;

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

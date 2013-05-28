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
package ca.phon.query.script;

import java.util.ArrayList;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.util.Range;

public class SIPARange extends SRange implements PhonexSearchable, CVSearchable,
	StressPatternSearchable {

	public SIPARange(Record record, Range r, int recordIndex, String tier, int gIndex) {
		super(record, r, recordIndex, tier, gIndex);
	}
	
	@Override
	public SRange[] findPlain(String txt) {
		return findPlain(txt, true);
	}
	
	public int getNumberOfPhones() {
		return getPhones().size();
	}
	
	public SIPAPhone getPhone(int pos) {
//		Range newRange = new Range(
//				range.getStart()+pos, range.getStart()+pos+1, true);
//		return new SIPAPhone(utt, newRange, uttIndex, tierName, gIndex);
		Range pRange = new Range(pos, pos+1, true);
		Range sRange = 
			Phone.convertPhoneRangetoStringRange(getPhones(), pRange);
		sRange.setStart(sRange.getStart()+range.getStart());
		sRange.setEnd(sRange.getEnd()+range.getStart());
		return new SIPAPhone(utt, sRange, uttIndex, tierName, gIndex);
	}
	
	public SIPARange getPhoneRange(int pos, int end) {
		int rS = range.getStart() + pos;
		int rE = range.getStart() + end;
		Range newRange = new Range(rS, rE);
		return new SIPARange(utt, newRange, uttIndex, tierName, gIndex);
	}
	
	protected IPATranscript getPhones() {
		IWord w = utt.getWords().get(gIndex);
		if(w == null) return new ArrayList<Phone>();
		
		List<Phone> phones =
			TranscriptUtils.getTierPhoneList(w, tierName);
		Range pRange = 
			Phone.convertStringRangeToPhoneRange(getData(), range);
		
		List<Phone> retVal = new ArrayList<Phone>();
		for(int pIndex:pRange) {
			//if(pIndex >= 0 && pIndex < phones.size())
				retVal.add(phones.get(pIndex));
		}
		
		return retVal;
	}
	
	@Override
	public SRange[] findPlain(String txt, boolean caseSensitive) {
		SRange[] ranges = super.findPlain(txt, caseSensitive);
		List<SRange> ipaRanges = new ArrayList<SRange>();
		for(SRange r:ranges) {
			// convert to an SIPARange
			SIPARange newRange = new SIPARange(utt, r.range, 
					uttIndex, tierName, gIndex);
			ipaRanges.add(newRange);
		}
		return ipaRanges.toArray(new SRange[0]);
	}
	
	@Override
	public SRange[] findRegex(String txt) {
		return findRegex(txt, true);
	}
	
	@Override
	public SRange[] findRegex(String txt, boolean caseSensitive) {
		SRange[] ranges = super.findRegex(txt, caseSensitive);
		List<SRange> ipaRanges = new ArrayList<SRange>();
		for(SRange r:ranges) {
			// convert to an SIPARange
			SIPARange newRange = new SIPARange(utt, r.range, 
					uttIndex, tierName, gIndex);
			ipaRanges.add(newRange);
		}
		return ipaRanges.toArray(new SRange[0]);
	}

	@Override
	public boolean containsPhonex(String txt) {
		List<Phone> searchPhones = getPhones();
		PhoneSequenceMatcher m = null;
		try {
			m = PhoneSequenceMatcher.compile(txt);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		
		return m.findWithin(searchPhones);
	}

	@Override
	public SRange[] findPhonex(String txt) {
		List<Phone> phones = getPhones();
		PhoneSequenceMatcher m = null;
		try {
			m = PhoneSequenceMatcher.compile(txt);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		
		List<SRange> retVal = new ArrayList<SRange>();
		
		List<Range> found = m.findRanges(phones);
		for(Range r:found) {
			Range convertedRange =
				Phone.convertPhoneRangetoStringRange(phones, r);
			
			Range realRange = new Range(convertedRange.getStart() + range.getStart(),
					convertedRange.getEnd() + range.getStart(), true);
			retVal.add(new SIPARange(utt, realRange, uttIndex, tierName, gIndex));
		}
		
		return retVal.toArray(new SRange[0]);
	}

	@Override
	public boolean matchesPhonex(String txt) {
		List<Phone> searchPhones = getPhones();
		PhoneSequenceMatcher m = null;
		try {
			m = PhoneSequenceMatcher.compile(txt);
		} catch (ParserException e) {
			return false;
		}
		
		return m.matches(searchPhones);
	}

	@Override
	public boolean containsCVType(String txt) {
		CVSeqPattern cvPattern = null;
		try {
			cvPattern = CVSeqPattern.compile(txt);
		} catch (ParserException pe) {
			return false;
		}
		
		String cvSeq = getCvPattern();
		List<CVSeqType> stTypes = 
			CVSeqType.toCVSeqMatcherList(cvSeq);
		return cvPattern.findWithin(stTypes);
	}

	@Override
	public SRange[] findCVType(String txt) {
		List<SRange> retVal = new ArrayList<SRange>();
		CVSeqPattern cvPattern = null;
		try {
			cvPattern = CVSeqPattern.compile(txt);
		} catch (ParserException pe) {
			return retVal.toArray(new SRange[0]);
		}
		
		List<Phone> phones = getPhones();
		String cvSeq = getCvPattern();
		List<CVSeqType> stTypes = 
			CVSeqType.toCVSeqMatcherList(cvSeq);
		
		List<Range> stRanges = cvPattern.findRanges(stTypes);
		for(Range range:stRanges) {
			Range phoneRange = 
				CVSeqPattern.convertCVRangeToPhoneRange(phones, range);
			Range realRange = 
				Phone.convertPhoneRangetoStringRange(phones, phoneRange);
			realRange.setStart(realRange.getStart()+super.range.getStart());
			realRange.setEnd(realRange.getEnd()+super.range.getStart());
			
			SRange r = new SIPARange(utt, realRange, uttIndex, tierName, gIndex);
			retVal.add(r);
		}
		
		return retVal.toArray(new SRange[0]);
	}

	@Override
	public boolean matchesCVType(String txt) {
		CVSeqPattern cvPattern = null;
		try {
			cvPattern = CVSeqPattern.compile(txt);
		} catch (ParserException pe) {
			return false;
		}
		
		String cvSeq = getCvPattern();
		List<CVSeqType> stTypes = 
			CVSeqType.toCVSeqMatcherList(cvSeq);
		return cvPattern.matches(stTypes);
	}

	public String getCvPattern() {
		return CVSeqPattern.getCVSeq(getPhones());
	}
	
	@Override
	public boolean containsStressPattern(String txt) {
		StressPattern sp = null;
		try {
			sp = StressPattern.compile(txt);
		} catch (ParserException pe) {
			return false;
		}
		
		String stressPattern = StressPattern.getStressPattern(getPhones());
		List<StressMatcherType> stTypes = 
			StressMatcherType.toStressMatcherList(stressPattern);
		return sp.findWithin(stTypes);
	}

	@Override
	public SRange[] findStressPattern(String txt) {
		List<SRange> retVal = new ArrayList<SRange>();
		StressPattern sp = null;
		try {
			sp = StressPattern.compile(txt);
		} catch (ParserException pe) {
			return retVal.toArray(new SRange[0]);
		}
		
		List<Phone> phones = getPhones();
		
		String stressPattern = StressPattern.getStressPattern(phones);
		List<StressMatcherType> stTypes = 
			StressMatcherType.toStressMatcherList(stressPattern);
		
		List<Range> stRanges = sp.findRanges(stTypes);
		for(Range range:stRanges) {
			Range phoneRange = 
				StressPattern.convertSPRToPR(phones, stressPattern, range);
			Range realRange = 
				Phone.convertPhoneRangetoStringRange(phones, phoneRange);
			realRange.setStart(realRange.getStart()+super.range.getStart());
			realRange.setEnd(realRange.getEnd()+super.range.getStart());
			
			SRange r = new SIPARange(utt, realRange, uttIndex, tierName, gIndex);
			retVal.add(r);
		}
		
		return retVal.toArray(new SRange[0]);
	}

	@Override
	public boolean matchesStressPattern(String txt) {
		StressPattern sp = null;
		try {
			sp = StressPattern.compile(txt);
		} catch (ParserException pe) {
			return false;
		}
		
		String stressPattern = getStressPattern();
		List<StressMatcherType> stTypes = 
			StressMatcherType.toStressMatcherList(stressPattern);
		return sp.matches(stTypes);
	}
	
	public String getStressPattern() {
		String stressPattern = StressPattern.getStressPattern(getPhones());
		return stressPattern;
	}
	
	/**
	 * The method overrides the .length operation in javascript
	 * which operates on the return value from toString().  Since
	 * we actually return the 'null' character for empty SIPARanges,
	 * we need to return the correct value here.
	 * @return the number of phones
	 */
	public int getLength() {
		return getNumberOfPhones();
	}

	@Override
	public String toString() {
		String retVal = super.toString();
		if(retVal.length() == 0) {
			retVal = "\u2205";
		}
		return retVal;
	}
	
}

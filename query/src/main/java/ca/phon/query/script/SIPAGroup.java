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

import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;

/**
 * 
 *
 */
public class SIPAGroup extends SIPARange implements ScriptGroup {
	
	private List<Range> wordRanges = new ArrayList<Range>();
	private List<Range> syllableRanges = new ArrayList<Range>();
	
	public SIPAGroup(Record record, Range r, int recordIndex, String tierName, int gIndex) {
		super(record, r, recordIndex, tierName, gIndex);	
	}

//	protected void updatePhones() {
//		// we still want to create the sub-word ranges
//		IPATranscript phones = super.getPhones();
//		wordRanges.clear();
//		
//		List<Phone> currentWord = new ArrayList<Phone>();
//		for(Phone p:phones) {
//			if(p.getScType() == SyllableConstituentType.WordBoundaryMarker) {
//				if(currentWord.size() == 0) continue;
//				Range phoneRange = new Range(
//						currentWord.get(0).getPhoneIndex(), currentWord.get(currentWord.size()-1).getPhoneIndex(),
//						false);
//				Range strRange = Phone.convertPhoneRangetoStringRange(phones, phoneRange);
//				wordRanges.add(strRange);
////				phonesByWord.add(currentWord);
//				currentWord = new ArrayList<Phone>();
//			} else {
//				currentWord.add(p);
//			}
//		}
//		if(currentWord.size() > 0) {
//			Range phoneRange = new Range(
//					currentWord.get(0).getPhoneIndex(), currentWord.get(currentWord.size()-1).getPhoneIndex(),
//					false);
//			Range strRange = Phone.convertPhoneRangetoStringRange(phones, phoneRange);
//			wordRanges.add(strRange);
//		}
//		
//		List<Syllable> syll = Syllabifier.getSyllabification(phones);
//		for(Syllable s:syll) {
//			int si = s.getPhones()[0].getPhoneIndex();
//			int ei = s.getPhones()[s.getPhones().length-1].getPhoneIndex();
//			
//			Range syllRange = new Range(si, ei, false);
//			Range pRange = Phone.convertPhoneRangetoStringRange(phones, syllRange);
//			
//			syllableRanges.add(pRange);
//		}
//	}

	@Override
	public ScriptWord getWord(int index) {
		if(index < 0 || index >= wordRanges.size()) {
//			return null;
			return new SIPAWord(record, new Range(0, 0, false), recordIndex, tierName, gIndex);
		}
		
		return new SIPAWord(record, wordRanges.get(index), recordIndex, tierName, gIndex);
	}
	
	@Override
	public int getNumberOfWords() {
		return wordRanges.size();
	}
	
	public int getNumberOfSyllables() {
		return this.syllableRanges.size();
	}
	
	public SSyllable getSyllable(int index) {
		if(index < 0 || index >= this.syllableRanges.size()) {
//			return null;
			return new SSyllable(record, new Range(0, 0, false), recordIndex, tierName, gIndex);
		}
		
		return new SSyllable(record, syllableRanges.get(index), recordIndex, tierName, gIndex);
	}

}

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
package ca.phon.syllabifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.phon.ipa.phone.Phone;
import ca.phon.syllabifier.io.ConstituentType;
import ca.phon.syllabifier.io.SonorityDirection;
import ca.phon.syllabifier.io.StageType;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Range;
import ca.phon.util.RangeComparator;

/**
 * Implements a stage for the syllabification
 * process.
 *
 */
public class Stage extends StageType {
	
	public List<Phone> run(List<Phone> tape, SonorityScale scale) {
		
		List<Range> tempList = new ArrayList<Range>();
		
		// step 1 - use the given phone constraint to identify phones of interest
		Constraint phoneOfInterestConstraint = 
			(Constraint)this.getPhoneOfInterest().getConstraint();
		List<Range> phoneRanges = phoneOfInterestConstraint.findRangesInList(tape);
		
		// step 2 - check previous phone(s)
		if(this.getPrevPhone() != null) {
			// check constraint
			if(this.getPrevPhone().getConstraint() != null) {
				Constraint prevPhoneConstraint = 
					(Constraint)getPrevPhone().getConstraint();
				tempList.clear();
				for(Range r:phoneRanges) {
					int prevPhoneIndex = r.getFirst()-1;
					
					List<Phone> testPhones = new ArrayList<Phone>();
					if(prevPhoneIndex >= 0) {
						Range prevRange = new Range(0, prevPhoneIndex);
						for(int pIndex:prevRange) testPhones.add(tape.get(pIndex));
					}
					
					if(testPhones.size() == 0 && prevPhoneConstraint.matchesEmptyList()) {
						tempList.add(r);
					} else {
						List<Range> foundConstraintRanges = 
							prevPhoneConstraint.findRangesInList(testPhones);
						for(Range cRange:foundConstraintRanges) {
							// if the range includes the final phone of
							// the test list, include the current range of interest
							if(cRange.contains(testPhones.size()-1))
								tempList.add(r);
						}
					}
				}
				phoneRanges.clear();
				phoneRanges.addAll(tempList);
			}
			
//			if(this.getPrevPhone().getSyllabificationStatus() != null) {
//				tempList.clear();
//				for(Range r:phoneRanges) {
//					int prevPhoneIndex = r.getFirst()-1;
//					if(prevPhoneIndex < 0) {
//						if(!this.getPrevPhone().isMustExist())
//							tempList.add(r);
//						continue;
//					}
//					
//					if(checkPhoneSyllabification(tape.get(prevPhoneIndex), 
//							getPrevPhone().getSyllabificationStatus()))
//						tempList.add(r);
//				}
//				phoneRanges.clear();
//				phoneRanges.addAll(tempList);
//			}
		}
		
		// step 3 - check next phone
		if(this.getNextPhone() != null) {
			// check constraint
			if(this.getNextPhone().getConstraint() != null) {
				Constraint nextPhoneConstraint = 
					(Constraint)getNextPhone().getConstraint();
				tempList.clear();
				for(Range r:phoneRanges) {
					int nextPhoneIndex = r.getFirst()+1;
//					if(nextPhoneIndex >= tape.size()) {
//						if(!this.getNextPhone().isMustExist())
//							tempList.add(r);
//						continue;
//					}
					
					List<Phone> testPhones = new ArrayList<Phone>();
					if(nextPhoneIndex <= tape.size()) {
						Range nextRange = new Range(nextPhoneIndex, tape.size(), true);
						for(int pIndex:nextRange) testPhones.add(tape.get(pIndex));
					}
					
					if(testPhones.size() == 0 && nextPhoneConstraint.matchesEmptyList()) {
						tempList.add(r);
					} else {
						List<Range> foundConstraintRanges = 
							nextPhoneConstraint.findRangesInList(testPhones);
						for(Range cRange:foundConstraintRanges) {
							// if the range includes the final phone of
							// the test list, include the current range of interest
							if(cRange.contains(0))
								tempList.add(r);
						}
					}
				}
				phoneRanges.clear();
				phoneRanges.addAll(tempList);
			}
			
//			if(this.getNextPhone().getSyllabificationStatus() != null) {
//				tempList.clear();
//				for(Range r:phoneRanges) {
//					int nextPhoneIndex = r.getFirst()+1;
//					if(nextPhoneIndex >= tape.size()) {
//						if(!this.getNextPhone().isMustExist())
//							tempList.add(r);
//						continue;
//					}
//					if(checkPhoneSyllabification(tape.get(nextPhoneIndex), 
//							getNextPhone().getSyllabificationStatus()))
//						tempList.add(r);
//				}
//				phoneRanges.clear();
//				phoneRanges.addAll(tempList);
//			}
		}
		
		// what are we trying to mark..
		SyllableConstituentType scType = 
			convertXMLConstituentType(this.getPhoneOfInterest().getMarkAs());
		
		// step 4 - confirm sonority
		if(this.isUseMDC()) {
			tempList.clear();
			if(this.getSonorityDirection() == SonorityDirection.RIGHT) {
				// rising sonority
				// Sonority(NextPhone) - Sonority(PhoneOfInterest) >= mdc
				for(Range r:phoneRanges) {
					int phoneOfInterestIndex = r.getLast();
					int nextPhoneIndex = phoneOfInterestIndex + 1;
					
					if(nextPhoneIndex >= tape.size()) {
						// add this range anyway
						tempList.add(r);
						continue;
					}
					
					Phone phoneOfInterest = tape.get(phoneOfInterestIndex);
					Phone nextPhone = tape.get(nextPhoneIndex);
					
					int distance = scale.calculateSonorityDistance(nextPhone, phoneOfInterest);
					if(distance >= this.getMdc() ||
							(this.isAllowsFlatSonority() && distance == 0)) {
						tempList.add(r);
					}
				}
			} else {
				// falling sonority
				// Sonority(PhoneOfInterest) - Sonority(PrevPhone) >= mdc
				for(Range r:phoneRanges) {
					int phoneOfInterestIndex = r.getLast();
					int prevPhoneIndex = phoneOfInterestIndex - 1;
					
					if(prevPhoneIndex < 0) {
						// add this range anyway
						tempList.add(r);
						continue;
					}
					
					Phone phoneOfInterest = tape.get(phoneOfInterestIndex);
					Phone prevPhone = tape.get(prevPhoneIndex);
					
					int distance = scale.calculateSonorityDistance(prevPhone, phoneOfInterest);
					if(distance >= this.getMdc() ||
							(this.isAllowsFlatSonority() && distance == 0)) {
						tempList.add(r);
					}
				}
			}
			phoneRanges.clear();
			phoneRanges.addAll(tempList);
		}
		
		// sort list
		Collections.sort(phoneRanges, new RangeComparator());
		
		// anything left in the phoneRanges list will be
		// marked with the OnPass syllabificationStatus
		for(Range r:phoneRanges) {
			boolean makeDip = false;
			for(int phoneIndex:r) {
				Phone currentPhone = tape.get(phoneIndex);
				
				if(makeDip &&  (currentPhone.getScType() == null || currentPhone.getScType() == SyllableConstituentType.UNKNOWN))
					currentPhone.setDiphthongMember(true);
				
				if(scType == SyllableConstituentType.NUCLEUS 
						&& (currentPhone.getScType() == null || currentPhone.getScType() == SyllableConstituentType.UNKNOWN))
					makeDip = true;
				
				currentPhone.setScType(scType);
			}
		}
		
		return tape;
	}

	private SyllableConstituentType convertXMLConstituentType(ConstituentType type) {
			if(type == ConstituentType.ONSET)
				return SyllableConstituentType.ONSET;
			else if(type == ConstituentType.NUCLEUS) 
				return SyllableConstituentType.NUCLEUS;
			else if(type == ConstituentType.CODA)
				return SyllableConstituentType.CODA;
			else if(type == ConstituentType.LEFT_APPENDIX)
				return SyllableConstituentType.LEFTAPPENDIX;
			else if(type == ConstituentType.RIGHT_APPENDIX)
				return SyllableConstituentType.RIGHTAPPENDIX;
			else if(type == ConstituentType.OEHS)
				return SyllableConstituentType.OEHS;
			else if(type == ConstituentType.AMBISYLLABIC)
				return SyllableConstituentType.AMBISYLLABIC;
			else if(type == ConstituentType.UNKNOWN)
				return SyllableConstituentType.UNKNOWN;
			else
				return SyllableConstituentType.UNKNOWN;
	}
	
	private boolean checkPhoneSyllabification(Phone phone, ConstituentType type) {
		if(type == ConstituentType.ONSET)
			return phone.getScType() == SyllableConstituentType.ONSET ||
				   phone.getScType() == SyllableConstituentType.AMBISYLLABIC;
		else if(type == ConstituentType.NUCLEUS) 
			return phone.getScType() == SyllableConstituentType.NUCLEUS;
		else if(type == ConstituentType.CODA)
			return phone.getScType() == SyllableConstituentType.CODA ||
				   phone.getScType() == SyllableConstituentType.AMBISYLLABIC;
		else if(type == ConstituentType.LEFT_APPENDIX)
			return phone.getScType() == SyllableConstituentType.LEFTAPPENDIX;
		else if(type == ConstituentType.RIGHT_APPENDIX)
			return phone.getScType() == SyllableConstituentType.RIGHTAPPENDIX;
		else if(type == ConstituentType.OEHS)
			return phone.getScType() == SyllableConstituentType.OEHS;
		else if(type == ConstituentType.AMBISYLLABIC)
			return phone.getScType() == SyllableConstituentType.AMBISYLLABIC;
		else if(type == ConstituentType.UNKNOWN)
			return phone.getScType() == SyllableConstituentType.UNKNOWN || phone.getScType() == null;
		else if(type == ConstituentType.DON_T_CARE)
			return true;
		else
			return false;
	}
	
}

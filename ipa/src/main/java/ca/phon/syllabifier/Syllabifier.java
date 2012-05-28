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
import java.util.List;

import ca.phon.ipa.phone.Phone;
import ca.phon.syllabifier.io.StageType;
import ca.phon.syllabifier.io.SyllabifierDef;
import ca.phon.syllable.Syllable;
import ca.phon.syllable.SyllableConstituentType;

/**
 * 
 * Class to perform syllabification based on a
 * syllabifier definition.
 * 
 *
 */
public class Syllabifier extends SyllabifierDef {
	
	/** Constructor */
	public Syllabifier() {
		super();
	}
	
	/**
	 * Return a list of syllables given a transcript.
	 * 
	 * @param transcript
	 * @return Syllable[]
	 */
	public List<Syllable> syllabify(List<Phone> phones) {
		List<Phone> lastList = new ArrayList<Phone>();
		for(StageType stage:getStage()) {
			Stage currentStage = (Stage)stage;
			
			if(currentStage.isContinueUntilFail()) {
				boolean hasChanged = true;
				while(hasChanged) {
					lastList = Phone.cloneList(phones);
					currentStage.run(phones, (SonorityScale)getSonorityScale());
					hasChanged = checkForChange(lastList, phones);
				}
			} else {
				lastList = Phone.cloneList(phones);
				currentStage.run(phones, (SonorityScale)getSonorityScale());
			}
		}

		for(Phone p:phones) {
			if(p.getFeatureSet().hasFeature("Untranscribed")) {
				p.setScType(SyllableConstituentType.UNKNOWN);
			}
		}
		
		return getSyllabification(phones);
	}

	/**
	 * Returns the syllabification as done in stages.
	 * 
	 * @param transcript
	 * @return ArrayList<SyllabificationStageResult>
	 */
	public ArrayList<SyllabificationStageResult> syllabifyByStages(List<Phone> phones) {
		ArrayList<SyllabificationStageResult> retVal =
			new ArrayList<SyllabificationStageResult>();
		
		List<Phone> lastList = new ArrayList<Phone>();
		for(StageType stage:getStage()) {
			Stage currentStage = (Stage)stage;
	
			if(currentStage.isContinueUntilFail()) {
				boolean hasChanged = true;
				while(hasChanged) {
					lastList = Phone.cloneList(phones);
					currentStage.run(phones, (SonorityScale)getSonorityScale());
					
					SyllabificationStageResult res = new SyllabificationStageResult();
					res.stageName = currentStage.getName();
					res.stageResult = Phone.cloneList(phones);
					retVal.add(res);
					
					hasChanged = checkForChange(lastList, phones);
				}
			} else {
				lastList = Phone.cloneList(phones);
				currentStage.run(phones, (SonorityScale)getSonorityScale());
				
				SyllabificationStageResult res = new SyllabificationStageResult();
				res.stageName = currentStage.getName();
				res.stageResult = Phone.cloneList(phones);
				retVal.add(res);
			}
		}
		
		return retVal;
	}

	public ArrayList<SyllabificationStageResult> syllabifyFromStage(
			List<Phone> phones, String stageName) {
		ArrayList<SyllabificationStageResult> retVal =
				new ArrayList<SyllabificationStageResult>();

		boolean hasStarted = false;
		List<Phone> lastList = Phone.cloneList(phones);
		for(StageType stage:getStage()) {
			Stage currentStage = (Stage)stage;

			if(!hasStarted) {
				if(currentStage.getName().equals(stageName)) {
					hasStarted = true;
				}
			} else {
				if(currentStage.isContinueUntilFail()) {
					boolean hasChanged = true;
					while(hasChanged) {
						lastList = Phone.cloneList(phones);
						currentStage.run(phones, (SonorityScale)getSonorityScale());

						SyllabificationStageResult res = new SyllabificationStageResult();
						res.stageName = currentStage.getName();
						res.stageResult = Phone.cloneList(phones);
						retVal.add(res);

						hasChanged = checkForChange(lastList, phones);
					}
				} else {
					lastList = Phone.cloneList(phones);
					currentStage.run(phones, (SonorityScale)getSonorityScale());

					SyllabificationStageResult res = new SyllabificationStageResult();
					res.stageName = currentStage.getName();
					res.stageResult = Phone.cloneList(phones);
					retVal.add(res);
				}
			}

		}

		return retVal;
	}
	
	private boolean checkForChange(List<Phone> list1, List<Phone> list2) {
		if(list1.size() != list2.size()) return false;
		
		boolean retVal = true;
		for(int i = 0; i < list1.size(); i++) {
			Phone a = list1.get(i);
			Phone b = list2.get(i);
			
			retVal &= a.getPhoneString().equals(b.getPhoneString());
			retVal &= a.getScType() == b.getScType();
			retVal &= a.isDiphthongMember() == b.isDiphthongMember();
		}
		
		return !retVal;
	}
	
	
	public static List<Syllable> getSyllabification(List<Phone> phones) {
		List<Syllable> retVal = new ArrayList<Syllable>();
		
		List<Phone> currentSyllable = new ArrayList<Phone>();
		for(Phone p:phones) {
			
			// lastPhone is last phone in current syllable
			Phone lastPhone = 
					(currentSyllable.size() > 0 ? currentSyllable.get(currentSyllable.size()-1) : null);
			
			if(p.getScType() == SyllableConstituentType.WORDBOUNDARYMARKER
					|| p.getScType() == SyllableConstituentType.SYLLABLEBOUNDARYMARKER) {
				// don't add phone to syllable, create a new one
				if(currentSyllable.size() > 0)
					retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
				currentSyllable.clear();
			} else if(p.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
				// new syllable start, add current syllable (if any) and
				// start a new one
				if(currentSyllable.size() > 0)
					retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
				currentSyllable.clear();
				currentSyllable.add(p);
			} else if(p.getScType() == SyllableConstituentType.LEFTAPPENDIX) {
				if(lastPhone != null) {
					// add only if last phone was a stress marker or
					// a LeftAppendix.  Otherwise start a new syllable
					if(lastPhone.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER
							|| lastPhone.getScType() == SyllableConstituentType.LEFTAPPENDIX) {
						currentSyllable.add(p);
					} else {
						if(currentSyllable.size() > 0)
							retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
						currentSyllable.clear();
						currentSyllable.add(p);
					}
				} else {
					currentSyllable.add(p);
				}
			} else if(p.getScType() == SyllableConstituentType.ONSET) {
				if(lastPhone != null) {
					// add only if the last phone was a stress marker, onset,
					// or leftappendix
					if(lastPhone.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER
							|| lastPhone.getScType() == SyllableConstituentType.LEFTAPPENDIX
							|| lastPhone.getScType() == SyllableConstituentType.ONSET) {
						currentSyllable.add(p);
					} else {
						if(currentSyllable.size() > 0)
							retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
						currentSyllable.clear();
						currentSyllable.add(p);
					}
				} else {
					currentSyllable.add(p);
				}
			} else if(p.getScType() == SyllableConstituentType.AMBISYLLABIC) {
				// ambisyllabic phones always start a new syllable
				if(currentSyllable.size() > 0)
					retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
				currentSyllable.clear();
				currentSyllable.add(p);
			} else if(p.getScType() == SyllableConstituentType.OEHS) {
				// OEHS phones always start a new syllable
				if(currentSyllable.size() > 0)
					retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
				currentSyllable.clear();
				currentSyllable.add(p);
			} else if(p.getScType() == SyllableConstituentType.NUCLEUS) {
				if(lastPhone != null) {
					// add to current syllable only if the previous phone
					// was not a 'right-hand' constituent type
					if(lastPhone.getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER
							|| lastPhone.getScType() == SyllableConstituentType.LEFTAPPENDIX
							|| lastPhone.getScType() == SyllableConstituentType.ONSET
							|| lastPhone.getScType() == SyllableConstituentType.AMBISYLLABIC
							|| lastPhone.getScType() == SyllableConstituentType.OEHS) {
						currentSyllable.add(p);
					} else if(lastPhone.getScType() == SyllableConstituentType.NUCLEUS) {
						// add to current syllable if we have a diphthong
						if(p.isDiphthongMember()) {
							currentSyllable.add(p);
						} else {
							if(currentSyllable.size() > 0)
								retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
							currentSyllable.clear();
							currentSyllable.add(p);
						}
					} else {
						if(currentSyllable.size() > 0)
							retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
						currentSyllable.clear();
						currentSyllable.add(p);
					}
				} else {
					currentSyllable.add(p);
				}
			} else if(p.getScType() == SyllableConstituentType.CODA) {
				if(lastPhone != null) {
					// add to current syllable only if the previous phone
					// was a Nucleus or Coda
					if(lastPhone.getScType() == SyllableConstituentType.NUCLEUS
							|| lastPhone.getScType() == SyllableConstituentType.CODA) {
						currentSyllable.add(p);
					} else {
						// ambisyllabic phones always start a new syllable
						if(currentSyllable.size() > 0)
							retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
						currentSyllable.clear();
						currentSyllable.add(p);
					}
				} else {
					currentSyllable.add(p);
				}
			} else if(p.getScType() == SyllableConstituentType.RIGHTAPPENDIX) {
				if(lastPhone != null) {
					// add to current syllable only if previous phone
					// was nucleus, coda or right appendix
					if(lastPhone.getScType() == SyllableConstituentType.NUCLEUS
							|| lastPhone.getScType() == SyllableConstituentType.CODA
							|| lastPhone.getScType() == SyllableConstituentType.RIGHTAPPENDIX) {
						currentSyllable.add(p);
					} else {
						// ambisyllabic phones always start a new syllable
						if(currentSyllable.size() > 0)
							retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
						currentSyllable.clear();
						currentSyllable.add(p);
					}
				} else {
					currentSyllable.add(p);
				}
			} else {
				// unknown phones always start a new syllable
				if(currentSyllable.size() > 0)
					retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
				currentSyllable.clear();
				currentSyllable.add(p);
			}
			
		}
		
		// add current syllable if non-empty
		if(currentSyllable.size() > 0)
			retVal.add(new Syllable(currentSyllable.toArray(new Phone[0])));
		
		
		// set syllable stress on phones
		int syllIndex = 0;
		for(Syllable s:retVal) {
			for(Phone p:s.getPhones()) {
				p.setSyllableIndex(syllIndex);
				p.setSyllablesInPhrase(retVal.size());
				p.setStress(s.getStress());
			}
			syllIndex++;
		}
		
		return retVal;
	}
	
	/**
	 * Return a list of lists of phones.  Each
	 * phone list is a syllable.
	 * @param phones the list of phones
	 * @return ArrayList<Phone[]>
	 */
//	public static List<Syllable> getSyllabification(List<Phone> phones) {
//		ArrayList<Syllable> syllList = new ArrayList<Syllable>();
//
//		int syllableStartIndex = -1;
//		int syllableEndIndex = -1;
//		SyllableConstituentType prevType = null;
//
//		for(int i = 0; i < phones.size(); i++) {
//			Phone currentPhone = phones.get(i);
//
//			syllableEndIndex = i;
//
//			if(currentPhone.getScType() == SyllableConstituentType.LeftAppendix) {
//				// left appendicies start new syllables except
//				// when the previous phone was also a LeftAppendix
//				if(prevType != SyllableConstituentType.LeftAppendix
//						&& prevType != SyllableConstituentType.SyllableStressMarker) {
//					if(syllableStartIndex >= 0) {
//						// add the new syllable
//						Phone[] syll = extractSyllable(
//								phones, syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					// set the new start index to i
//					syllableStartIndex = i;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.Onset) {
//				// onsets start new syllables except
//				// when the previous phone was a LA or Onset
//				if(prevType != SyllableConstituentType.LeftAppendix &&
//						prevType != SyllableConstituentType.Onset &&
//						prevType != SyllableConstituentType.SyllableStressMarker) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll = extractSyllable(
//								phones, syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					syllableStartIndex = i;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.Nucleus) {
//				// nuclei start new syllables except
//				// when the previous phone was a LA, Onset or If we are in the middle
//				// of an onset
//				if(prevType != SyllableConstituentType.LeftAppendix &&
//						prevType != SyllableConstituentType.Onset &&
//						prevType != SyllableConstituentType.SyllableStressMarker &&
//						prevType != SyllableConstituentType.Ambisyllabic) {
//
//					if(!(prevType == SyllableConstituentType.Nucleus
//							&& currentPhone.isDiphthongMember())) {
//
//						if(syllableStartIndex >= 0) {
//							Phone[] syll = extractSyllable(
//									phones, syllableStartIndex, syllableEndIndex);
//							syllList.add(new Syllable(syll));
//						}
//
//						syllableStartIndex = i;
//					}
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.Coda) {
//				// codas will only start new syllables if the previous phone
//				// was an unknown or RA
//				if(prevType == null || prevType == SyllableConstituentType.Unknown ||
//						prevType == SyllableConstituentType.RightAppendix) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll = extractSyllable(
//								phones, syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					syllableStartIndex = i;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.RightAppendix) {
//				// right appendicies will only begin new syllables if the
//				// previous phone was an unknown
//				if(prevType == null || prevType == SyllableConstituentType.Unknown) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll =
//							extractSyllable(phones, syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					syllableStartIndex = i;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.SyllableBoundaryMarker
//					|| currentPhone.getScType() == SyllableConstituentType.WordBoundaryMarker) {
//				// stop the current syllable, but don't add this phone
//				// to the next
//				if(prevType != SyllableConstituentType.Unknown) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll =
//							extractSyllable(phones, syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					syllableStartIndex = -1;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.OEHS) {
//				// if previous was also an oehs, add to syllable, otherwise start a new one
//				if(prevType != SyllableConstituentType.OEHS) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll =
//							extractSyllable(phones,syllableStartIndex, syllableEndIndex);
//						syllList.add(new Syllable(syll));
//					}
//
//					// set new syllable start index to i
//					syllableStartIndex = i;
//				}
//			} else if(currentPhone.getScType() == SyllableConstituentType.Ambisyllabic) {
//				// we detect ambisyllabicity at the end of the first syllable
//				// add this new syllable to the list and also include this phone
//				// in the new syllable
//				if(prevType != SyllableConstituentType.Ambisyllabic) {
//					if(syllableStartIndex >= 0) {
//						Phone[] syll =
//							extractSyllable(phones, syllableStartIndex, i+1);
//						syllList.add(new Syllable(syll));
//					}
//
//					syllableStartIndex = i;
//				}
//			} else {
//				// this 'catch all' clause includes
//				// unknowns, stress and boundary markers
//				// which always begin new syllables
//				if(syllableStartIndex >= 0) {
//					Phone[] syll =
//						extractSyllable(phones, syllableStartIndex, syllableEndIndex);
//					syllList.add(new Syllable(syll));
//				}
//
//				syllableStartIndex = i;
//			}
//
//			prevType = currentPhone.getScType();
//		}
//
//		// add the final syllable
//		if(syllableStartIndex >= 0) {
//			Phone[] syll =
//				extractSyllable(phones, syllableStartIndex, syllableEndIndex+1);
//			syllList.add(new Syllable(syll));
//		}
//		
//		// set syllable stress on phones
//		int syllIndex = 0;
//		for(Syllable s:syllList) {
//			for(Phone p:s.getPhones()) {
//				p.setSyllableIndex(syllIndex);
//				p.setSyllablesInPhrase(syllList.size());
//				p.setStress(s.getStress());
//			}
//			syllIndex++;
//		}
//		
//		return syllList;
//	}
	
	private static Phone[] extractSyllable(List<Phone> phones, int startIndex, int endIndex) {
		int syllSize = endIndex - startIndex;
		Phone[] retVal = new Phone[syllSize];
		
		for(int i = 0; i < syllSize; i++)
			retVal[i] = phones.get(startIndex+i);
		
		return retVal;
	}
	
	public static class SyllabificationStageResult {
		public String stageName;
		public List<Phone> stageResult;
	}

}

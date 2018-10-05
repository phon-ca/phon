/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.ipa;

/**
 * A factory for creating various types of {@link IPAElement}
 * objects.
 * 
 */
public class IPAElementFactory {

	/* Basic phones */
	/**
	 * New basic phone
	 */
	public Phone createPhone() {
		return new Phone();
	}
	
	/**
	 * Create a new basic phone 
	 * 
	 * @param basePhone
	 * @return the created {@link IPAElement} object
	 */
	public Phone createPhone(Character basePhone) {
		return new Phone(basePhone);
	}
	
	public Phone createPhone(Character basePhone, Diacritic[] combiningDiacritics) {
		return new Phone(new Diacritic[0], basePhone, combiningDiacritics, new Diacritic[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 */
	public Phone createPhone(Diacritic prefix, Character basePhone) {
		return new Phone(new Diacritic[]{prefix}, basePhone, new Diacritic[0], new Diacritic[0]);
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param suffix
	 */
	public Phone createPhone(Diacritic prefix, Character basePhone, Diacritic suffix) {
		return new Phone(new Diacritic[]{prefix}, basePhone, new Diacritic[0], new Diacritic[]{suffix});
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param combining
	 * @param suffix
	 */
	public Phone createPhone(Diacritic prefix, Character basePhone, Diacritic[] combining, Diacritic suffix) {
		return new Phone(new Diacritic[]{prefix}, basePhone, combining, new Diacritic[]{suffix});
	}
	
	/**
	 * Create a new basic phone
	 * 
	 * @param prefix
	 * @param basePhone
	 * @param combining
	 * @param suffix
	 */
	public Phone createPhone(Diacritic[] prefix, Character basePhone, Diacritic[] combining, Diacritic[] suffix) {
		return new Phone(prefix, basePhone, combining, suffix);
	}
	
	public Diacritic createDiacritic(Character dia) {
		return new Diacritic(dia);
	}
	
	public Diacritic createDiacritic(Diacritic[] prefix, Character dia, Diacritic[] suffix) {
		return new Diacritic(prefix, dia, suffix);
	}
	
	/**
	 * Create an empty compound phone
	 */
	public CompoundPhone createCompoundPhone() {
		return new CompoundPhone();
	}
	
	/**
	 * Create a new compound phone
	 * 
	 * @param phone1
	 * @param phone2
	 * @param ligature
	 */
	public CompoundPhone createCompoundPhone(Phone phone1, Phone phone2, Character ligature) {
		return new CompoundPhone(phone1, phone2, ligature);
	}
	
	/**
	 * Create a 'hard' syllable boundary. I.e., a '.'
	 * 
	 * @return a syllable boundary marker
	 */
	public SyllableBoundary createSyllableBoundary() {
		return  new SyllableBoundary();
	}
	
	/**
	 * Create a new stress marker
	 * 
	 * @param type
	 * @return a new stress marker of the given
	 *  type
	 */
	public StressMarker createStress(StressType type) {
		return new StressMarker(type);
	}
	
	/**
	 * Create a primary stress marker
	 * 
	 * @return a new primary stress marker
	 */
	public StressMarker createPrimaryStress() {
		return new StressMarker(StressType.PRIMARY);
	}
	
	/**
	 * Create a secondary stress marker
	 * 
	 * @return a new secondary stress marker
	 */
	public StressMarker createSecondaryStress() {
		return new StressMarker(StressType.SECONDARY);
	}
	
	/**
	 * Create a new intonation group
	 * 
	 * @param type
	 * @return a new intonation group marker
	 */
	public IntonationGroup createIntonationGroup(IntonationGroupType type) {
		return new IntonationGroup(type);
	}
	
	/**
	 * Create a major intonation group marker
	 * 
	 * @return a new major intonation group marker
	 */
	public IntonationGroup createMajorIntonationGroup() {
		return new IntonationGroup(IntonationGroupType.MAJOR);
	}
	
	/**
	 * Create a minor intonation group marker
	 * 
	 * @return a new minor intonation group marker
	 */
	public IntonationGroup createMinorIntonationGroup() {
		return new IntonationGroup(IntonationGroupType.MINOR);
	}
	
	/**
	 * Create a new word boundary. I.e., a ' '
	 * 
	 * @return a new word boundary marker
	 */
	public WordBoundary createWordBoundary() {
		return new WordBoundary();
	}
	
	/**
	 * Create a pause
	 * 
	 * @param length
	 * @return a new pause of the given length
	 */
	public Pause createPause(PauseLength length) {
		return new Pause(length);
	}
	
	/**
	 * Create an intra-word pause
	 * 
	 * @return new intra-word pause
	 */
	public IntraWordPause createIntraWordPause() {
		return new IntraWordPause();
	}
	
	/**
	 * Create a compound word marker.
	 * 
	 * @return compound word marker
	 */
	public CompoundWordMarker createCompoundWordMarker() {
		return new CompoundWordMarker();
	}
	
	/**
	 * Create a linker
	 * 
	 * @return linker (sandhi)
	 */
	public Linker createLinker() {
		return new Linker();
	}
	
	/**
	 * Create a contraction
	 * 
	 * @return contraction (sandhi)
	 */
	public Contraction createContraction() {
		return new Contraction();
	}

	/**
	 * Create a sandhi element based on given text
	 * 
	 * @return sandhi
	 */
	public Sandhi createSandhi(String text) {
		if(text.equals("\u203f")) {
			return createContraction();
		} else if(text.equals("\u2040")) {
			return createLinker();
		} else {
			return null;
		}
	}
	
	/**
	 * Create a phonex matcher reference.
	 * 
	 * @param groupIndex
	 */
	public PhonexMatcherReference createPhonexMatcherReference(int groupIndex) {
		return new PhonexMatcherReference(groupIndex);
	}
	
	/**
	 * Create a phonex matcher reference.
	 * 
	 * @param groupName
	 * @return
	 */
	public PhonexMatcherReference createPhonexMatcherReference(String groupName) {
		return new PhonexMatcherReference(groupName);
	}
	
	/**
	 * Create a new alignment marker (left-right arrow 0x2194)
	 * 
	 * @return
	 */
	public AlignmentMarker createAlignmentMarker() {
		return new AlignmentMarker();
	}
}

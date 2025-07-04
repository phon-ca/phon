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
package ca.phon.ipa;

import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;
import ca.phon.syllable.*;

import java.util.ArrayList;
import java.util.List;

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
		
	public Phone clonePhone(Phone p) {
		if(p instanceof CompoundPhone)
			return cloneCompoundPhone((CompoundPhone)p);
		else
			return new Phone(p.getPrefixDiacritics(), p.getBasePhone(), p.getCombiningDiacritics(), p.getSuffixDiacritics());
	}

	/**
	 * <p>Create diacritic from given string.
	 * Given string must match one of:
	 * <ul>
	 *    <li>PREFIX COMBINING*</li>
	 *    <li>SUFFIX ROLE_REVERSAL COMBINING*</li>
	 *    <li>SUFFIX COMBINING* LIGATURE</li>
	 *    <li>SUFFIX COMBINING*</li>
	 *    <li>PREFIX ROLE_REVERSAL COMBINING*</li>
	 *    <li>LIGATURE PREFIX COMBINING*</li>
	 * </ul>
	 * </p>
	 *
	 * @param text
	 * @return diacritic for given string
	 * @throws IllegalArgumentException if text does not match prefix/suffix diacritic rules
	 */
	public Diacritic createDiacritic(String text) {
		final IPATokens tokens = IPATokens.getSharedInstance();

		if(text.length() == 1) {
			return createDiacritic(text.charAt(0));
		} else {
			Diacritic[] prefix = new Diacritic[0];
			Character dia = null;
			Diacritic[] suffix = new Diacritic[0];

			IPATokenType firstType = tokens.getTokenType(text.charAt(0));
			switch (firstType) {
				case PREFIX_DIACRITIC, SUFFIX_DIACRITIC:
					dia = text.charAt(0);
					suffix = new Diacritic[text.length()-1];
					for(int i = 1; i < text.length(); i++) suffix[i-1] = createDiacritic(text.charAt(i));
					break;

				case LIGATURE:
					prefix = new Diacritic[]{ createDiacritic(text.charAt(0)) };
					dia = text.charAt(1);
					suffix = new Diacritic[text.length()-2];
					for(int i = 2; i < text.length(); i++) suffix[i-2] = createDiacritic(text.charAt(i));
					break;

				default:
					throw new IllegalArgumentException("Invalid diacritic text " + text);
			}
			return createDiacritic(prefix, dia, suffix);
		}
	}

	/**
	 * Create diacritic from given char
	 *
	 * @param dia
	 * @return
	 */
	public Diacritic createDiacritic(Character dia) {
		return new Diacritic(dia);
	}

	/**
	 * Create diacritic from prefix diacritics, dia, and suffix (or combining) diacritics
	 *
	 * @param prefix
	 * @param dia
	 * @param suffix
	 * @return
	 */
	public Diacritic createDiacritic(Diacritic[] prefix, Character dia, Diacritic[] suffix) {
		return new Diacritic(prefix, dia, suffix);
	}

	public Diacritic copyDiacritic(Diacritic dia) {
		return new Diacritic(dia.getPrefixDiacritics(), dia.getCharacter(), dia.getSuffixDiacritics());
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
	
	public CompoundPhone cloneCompoundPhone(CompoundPhone cp) {
		return new CompoundPhone(clonePhone(cp.getFirstPhone()), clonePhone(cp.getSecondPhone()), cp.getLigature());
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
	
	public StressMarker cloneStress(StressMarker sm) {
		return new StressMarker(sm.getType());
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
	
	public IntonationGroup cloneIntonationGroup(IntonationGroup ig) {
		return new IntonationGroup(ig.getType());
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

	public Pause createPause(float secs) {
		return new Pause(PauseLength.NUMERIC, secs);
	}
	
	public Pause clonePause(Pause pause) {
		return new Pause(pause.getType(), pause.getLength());
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
	 * Create the default compound word marker ('+')
	 * 
	 * @return compound word marker
	 */
	public CompoundWordMarker createCompoundWordMarker() {
		return createCompoundWordMarkerPlus();
	}

	public CompoundWordMarker createCompoundWordMarkerPlus() {
		return new CompoundWordMarker();
	}

	public CompoundWordMarker createCompoundWordMarkerTilde() {
		return new CompoundWordMarker('~');
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
		} else {
			return createLinker();
		}
	}
	
	public Sandhi cloneSandhi(Sandhi s) {
		if(s instanceof Contraction)
			return createContraction();
		else 
			return createLinker();
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
	
	public PhonexMatcherReference clonePhonexMatcherReference(PhonexMatcherReference ref) {
		if(ref.getGroupIndex() >= 0)
			return new PhonexMatcherReference(ref.getGroupIndex());
		else
			return new PhonexMatcherReference(ref.getGroupName());
	}
	
	/**
	 * Create a new alignment marker (left-right arrow 0x2194)
	 * 
	 * @return
	 */
	public AlignmentMarker createAlignmentMarker() {
		return new AlignmentMarker();
	}
	
	/**
	 * Clone the given {@link IPAElement} the returned element will have the
	 * exact content and same {@link SyllableConstituentType} as the
	 * original element
	 * 
	 * @param ele
	 * @return a copy of the given element
	 * 
	 * @throws NullPointerException if ele is <code>null</code>
	 */
	public IPAElement cloneElement(IPAElement ele) {
		IPAElement retVal = null;
		
		if(ele == null)
			throw new NullPointerException();
		
		if(ele instanceof Phone) {
			// also takes care of CompundPhones
			retVal = clonePhone((Phone)ele);
		} else if(ele instanceof Diacritic) {
			retVal = copyDiacritic((Diacritic)ele);
		} else if(ele instanceof SyllableBoundary) {
			retVal = createSyllableBoundary();
		} else if(ele instanceof StressMarker) {
			retVal = cloneStress((StressMarker)ele);
		} else if(ele instanceof IntonationGroup) {
			retVal = cloneIntonationGroup((IntonationGroup)ele);
		} else if(ele instanceof WordBoundary) {
			retVal = createWordBoundary();
		} else if(ele instanceof Pause) {
			retVal = clonePause((Pause)ele);
		} else if(ele instanceof IntraWordPause) {
			retVal = createIntraWordPause();
		} else if(ele instanceof CompoundWordMarker) {
			retVal = createCompoundWordMarker();
		} else if(ele instanceof Sandhi) {
			retVal = cloneSandhi((Sandhi)ele);
		} else if(ele instanceof PhonexMatcherReference) {
			retVal = clonePhonexMatcherReference((PhonexMatcherReference)ele);
		} else if(ele instanceof AlignmentMarker) {
			retVal = createAlignmentMarker();
		}
		
		copySyllabification(ele, retVal);
		
		return retVal;
	}
	
	/**
	 * Copy syllabification information from ele1 to ele2
	 * 
	 * @param ele1
	 * @param ele2
	 */
	public void copySyllabification(IPAElement ele1, IPAElement ele2) {
		// copy syllabification info
		SyllabificationInfo syllInfo = ele1.getExtension(SyllabificationInfo.class);
		SyllabificationInfo retInfo = ele2.getExtension(SyllabificationInfo.class);
		
		retInfo.setConstituentType(syllInfo.getConstituentType());
		retInfo.setDiphthongMember(syllInfo.isDiphthongMember());
		retInfo.setStress(syllInfo.getStress());
		retInfo.setToneNumber(syllInfo.getToneNumber());
	}
	
}

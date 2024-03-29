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
package ca.phon.syllable;

import ca.phon.extensions.Extension;
import ca.phon.ipa.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adds syllabification information to Phones.
 * 
 * Syllabification information includes:
 *  
 * <ul>
 *   <li># of syllables in phrase</li>
 *   <li>syllable index of phone</li>
 *   <li>syllable stress</li>
 *   <li>constituent type</li>
 * </ul>
 */
@Extension(IPAElement.class)
public class SyllabificationInfo {
	
	/**
	 * Assign stress and tonal information to individual {@link IPAElement}s.
	 * 
	 * @param phones
	 */
	public static void setupSyllabificationInfo(IPATranscript ipa) {
		if(ipa.getExtension(InfoFlag.class) == null) {
			setupStressInfo(ipa);
			setupToneNumber(ipa);
			InfoFlag flag = new InfoFlag();
			flag.complete = true;
			ipa.putExtension(InfoFlag.class, flag);
		}
	}
	
	private static void setupStressInfo(IPATranscript ipa) {
		ipa.syllables().parallelStream().forEach( (syll) -> {
			if(syll.length() > 0 && syll.elementAt(0).getScType() == SyllableConstituentType.SYLLABLESTRESSMARKER) {
				final StressMarker marker = (StressMarker)syll.elementAt(0);
				syll.forEach( (ele) -> {
					final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
					info.setStress( (marker.getType() == StressType.PRIMARY ? SyllableStress.PrimaryStress
							: SyllableStress.SecondaryStress) );
				});
			}
		});
	}
	
	private static void setupToneNumber(IPATranscript ipa) {
		ipa.syllables().parallelStream().forEach( (syll) -> {
			StringBuffer buf = new StringBuffer();
			for(IPAElement ele:syll) {
				Diacritic[] toneNumberEles = (ele instanceof Phone ? ((Phone)ele).getToneNumberDiacritics() : new Diacritic[0]);
				Arrays.stream(toneNumberEles).forEach( dia -> buf.append(diacriticToToneNumber(dia)) );
			}
			if(buf.length() > 0) {
				for(IPAElement ele:syll) {
					final SyllabificationInfo info = ele.getExtension(SyllabificationInfo.class);
					info.setToneNumber(buf.toString());
				}
			}
		});
	}
	
	private static String diacriticToToneNumber(Diacritic dia) {
		if(dia.getFeatureSet().hasFeature("tone0")) {
			return "0";
		} else if(dia.getFeatureSet().hasFeature("tone1")) {
			return "1";
		} else if(dia.getFeatureSet().hasFeature("tone2")) {
			return "2";
		} else if(dia.getFeatureSet().hasFeature("tone3")) {
			return "3";
		} else if(dia.getFeatureSet().hasFeature("tone4")) {
			return "4";
		} else if(dia.getFeatureSet().hasFeature("tone5")) {
			return "5";
		} else if(dia.getFeatureSet().hasFeature("tone6")) {
			return "6";
		} else if(dia.getFeatureSet().hasFeature("tone7")) {
			return "7";
		} else if(dia.getFeatureSet().hasFeature("tone8")) {
			return "8";
		} else if(dia.getFeatureSet().hasFeature("tone9")) {
			return "9";
		} else {
			return "";
		}
	}
	
	private static class InfoFlag  {
		private boolean complete = false;
	}
	
	/**
	 * Property name for constituent type changes
	 */
	public final static String PHONE_SCTYPE = "_sctype_";
	
	/**
	 * Property name for stress changes
	 */
	public final static String PHONE_STRESS = "_stress_";
	
	/**
	 * Property name for dipthong member changes
	 */
	public final static String PHONE_DIPHTHONG_MEMBER = "_diphthong_member_";
	
	/**
	 * The constituent type
	 */
	private SyllableConstituentType scType = SyllableConstituentType.UNKNOWN;
	
	/**
	 * Stress
	 */
	private SyllableStress stress = SyllableStress.NoStress;
	
	/**
	 * Is diphthong member?
	 * If <code>true</code>adjacent nuclei will be
	 * part of the same syllable.
	 */
	private boolean isDipththongMember = false;
	
	/**
	 * Tone features for syllable
	 */
	private String toneNumber = "";
	
	/**
	 * weak reference to parent
	 */
	private final AtomicReference<IPAElement> phoneRef;
	
	public SyllabificationInfo(IPAElement phone) {
		this(phone, SyllableConstituentType.UNKNOWN);
	}
	
	public SyllabificationInfo(IPAElement phone, SyllableConstituentType scType) {
		phoneRef = new AtomicReference<IPAElement>(phone);
		this.scType = scType;
	}
	
	/**
	 * Get parent {@link IPAElement}
	 * 
	 * @return parent phone, may be <code>null</code>
	 *  if the {@link WeakRefernce} to the parent is no 
	 *  longer valid.
	 */
	public IPAElement getPhone() {
		return phoneRef.get();
	}

	
	/**
	 * Return the syllable constituent type.
	 * 
	 * @return the syllable constituent type for
	 *  the phone
	 */
	public SyllableConstituentType getConstituentType() {
		return scType;
	}
	
	/**
	 * Set constituent type
	 * 
	 * @param scType
	 */
	public void setConstituentType(SyllableConstituentType scType) {
		final SyllableConstituentType oldType = this.scType;
		this.scType = scType;
		getPhone().firePropertyChange(PHONE_SCTYPE, oldType, this.scType);
	}
	
	public String getToneNumber() {
		return this.toneNumber;
	}
	
	public void setToneNumber(String toneNumber) {
		if(toneNumber.matches("[0-9]*")) {
			this.toneNumber = toneNumber;
		} else {
			throw new IllegalArgumentException(toneNumber);
		}
	}
	
	/**
	 * Stress of the parent syllable.
	 * 
	 * @return the stress type of the parent syllable
	 */
	public SyllableStress getStress() {
		return stress;
	}
	
	/**
	 * Set the stress of the parent syllable
	 * 
	 * @param stress
	 */
	public void setStress(SyllableStress stress) {
		final SyllableStress oldStress = this.stress;
		this.stress = stress;
		getPhone().firePropertyChange(PHONE_STRESS, oldStress, stress);
	}
	
	/**
	 * Is this phone part of a diphthong?
	 * 
	 * @return <code>true</code> if the constituent type of the
	 *  {@link IPAElement} is {@link SyllableConstituentType#NUCLEUS} and
	 *  the {@link IPAElement} is part of a diphthong, <code>false</code>
	 *  otherwise
	 */
	public boolean isDiphthongMember() {
		return (getConstituentType() == SyllableConstituentType.NUCLEUS && this.isDipththongMember);
	}
	
	/**
	 * Set this {@link IPAElement} as part of a diphthong member.
	 * Only valid if {@link #getConstituentType()} is
	 * {@link SyllableConstituentType#NUCLEUS}.
	 * 
	 * @param isDiphthongMember
	 */
	public void setDiphthongMember(boolean isDiphthongMember) {
		final boolean wasDiphthongMember = this.isDipththongMember;
		this.isDipththongMember = isDiphthongMember;
		getPhone().firePropertyChange(PHONE_DIPHTHONG_MEMBER, wasDiphthongMember, isDiphthongMember);
	}

}

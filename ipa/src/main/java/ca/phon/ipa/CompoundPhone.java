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

import java.util.ArrayList;

import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

/**
 * A compound Phone consists of two phones connected
 * by a ligature.
 * 
 */
public final class CompoundPhone extends Phone {
	
	/**
	 * First phone
	 */
	private Phone firstPhone;
	
	/**
	 * Second phone
	 */
	private Phone secondPhone;
	
	/**
	 * Ligature
	 */
	private Character ligature;
	
	public CompoundPhone() {
		super();
	}
	
	/**
	 * Constructor
	 * 
	 * @param firstPhone
	 * @param secondPhone
	 * @param ligature
	 */
	CompoundPhone(Phone firstPhone, Phone secondPhone, Character ligature) {
		super();
		setFirstPhone(firstPhone);
		setSecondPhone(secondPhone);
		setLigature(ligature);
	}
	
	/**
	 * Get the first phone in this compound 
	 * 
	 * @return the first phone
	 */
	public Phone getFirstPhone() {
		return this.firstPhone;
	}
	
	/**
	 * Set the first phone for this compound
	 * 
	 * @param phone
	 */
	public void setFirstPhone(Phone phone) {
		this.firstPhone = phone;
	}
	
	/**
	 * Get the base for the compound phone
	 *
	 * @return the base of the first phone plus the base of the second phone
	 */
	public String getBase() {
		return getFirstPhone().getBase() + getSecondPhone().getBase();
	}
	
	/**
	 * Get second phone
	 * 
	 * @return the second phone
	 */
	public Phone getSecondPhone() {
		return this.secondPhone;
	}

	/**
	 * Set second phone
	 * 
	 * @param phone
	 */
	public void setSecondPhone(Phone phone) {
		this.secondPhone = phone;
	}
	
	/**
	 * Get the ligature used
	 * 
	 * @return the ligature glyph
	 */
	public Character getLigature() {
		return this.ligature;
	}
	
	/**
	 * Set the ligature 
	 * 
	 * @param ligature
	 */
	public void setLigature(Character ligature) {
		IPATokenType tt = 
				IPATokens.getSharedInstance().getTokenType(ligature);
		if(tt == null && tt != IPATokenType.LIGATURE) {
			throw new IllegalArgumentException("ligature must have the token type LIGATURE");
		}
		this.ligature = ligature;
	}
	
//	@Override
//	public Diacritic[] getToneNumberDiacritics() {
//		ArrayList<Diacritic> diacritics = new ArrayList<>();
//		
//		Diacritic[] p1Diacritics = getFirstPhone().getToneNumberDiacritics();
//		for(Diacritic d:p1Diacritics) diacritics.add(d);
//
//		Diacritic[] p2Diacritics = getSecondPhone().getToneNumberDiacritics();
//		for(Diacritic d:p2Diacritics) diacritics.add(d);		
//		
//		return diacritics.toArray(new Diacritic[0]);
//	}

	@Override
	protected FeatureSet _getFeatureSet() {
		FeatureSet retVal = FeatureSet.union(firstPhone.getFeatureSet(), secondPhone.getFeatureSet());
		retVal = FeatureSet.union(retVal, getPrefixFeatures());
		retVal = FeatureSet.union(retVal, getCombiningFeatures());
		retVal = FeatureSet.union(retVal, getSuffixFeatures());
		
		// when the same consonant, add the long feature
		if(retVal.hasFeature("c") && firstPhone.getBasePhone() != null 
				&& firstPhone.getBasePhone().equals(secondPhone.getBasePhone())) {
			retVal = FeatureSet.union(retVal, FeatureSet.singleonFeature("long"));
		}
		
		return retVal;
	}

	@Override
	public String getText() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getPrefix());
		sb.append(getFirstPhone().getText());
		sb.append(ligature);
		sb.append(getSecondPhone().getText());
		sb.append(getSuffix());
		return sb.toString();
	}

}

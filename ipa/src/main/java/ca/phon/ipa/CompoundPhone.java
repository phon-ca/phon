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
package ca.phon.ipa;

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
	
	@Override
	protected FeatureSet _getFeatureSet() {
		FeatureSet retVal = FeatureSet.union(firstPhone.getFeatureSet(), secondPhone.getFeatureSet());
		
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

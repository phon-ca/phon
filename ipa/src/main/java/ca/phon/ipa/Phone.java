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

import java.util.*;

import ca.phon.ipa.features.*;
import ca.phon.ipa.parser.*;

/**
 * <p>A basic phone consists of the following parts:
 * 
 * <ul>
 * <li>A (optional) prefix diacritic</li>
 * <li>A base glyph (i.e., Consonant, Vowel, etc.)</li>
 * <li>Combining diacritics</li>
 * <li>Length - measured from 0-3, with 0 being no length modifier</li>
 * <li>A (optional) suffix diacritic</li>
 * </ul>
 */
public class Phone extends IPAElement {
	
	private Diacritic[] prefixDiacritics = new Diacritic[0];
	
	private Diacritic[] suffixDiacritics = new Diacritic[0];
	
	private Diacritic[] combiningDiacritics = new Diacritic[0];
	
	private Character basePhone;
	
	/**
	 * Create a new empty phone object.
	 * 
	 */
	Phone() {
		this('x');
	}
	
	/**
	 * Create a new Phone for the given base
	 * 
	 * @param basePhone
	 */
	Phone(Character basePhone) {
		super();
		setBasePhone(basePhone);
	}
	
	/**
	 * Full constructor
	 * 
	 * @param prefixDiacritics
	 * @param basePhone
	 * @param combiningDiacritics
	 * @param suffixDiacritics
	 */
	Phone(Diacritic[] prefixDiacritics, Character basePhone,
			Diacritic[] combiningDiacritics,
			Diacritic[] suffixDiacritics) {
		super();
		if(prefixDiacritics != null)
			setPrefixDiacritics(prefixDiacritics);
		setBasePhone(basePhone);
		if(combiningDiacritics != null)
			setCombiningDiacritics(combiningDiacritics);
		if(suffixDiacritics != null)
			setSuffixDiacritics(suffixDiacritics);
	}

	/* Get/Set methods */
	public Diacritic[] getPrefixDiacritics() {
		return prefixDiacritics;
	}

	/**
	 * Set the prefix diacritics for this Phone.
	 * 
	 * @param prefixDiacritics
	 */
	public void setPrefixDiacritics(Diacritic[] prefixDiacritics) {
		this.prefixDiacritics = prefixDiacritics;
	}
	
	/**
	 * Get the string representing this phone's prefix.
	 * 
	 * @return
	 */
	public String getPrefix() {
		final StringBuilder sb = new StringBuilder();
		for(Diacritic dia:getPrefixDiacritics()) {
			sb.append(dia.getText());
		}
		return sb.toString();
	}
	
	/**
	 * Get the feature set for the prefix diacritic.
	 * 
	 * @return feature set for the prefix diacritic or
	 *  an empty set if not found
	 */
	public FeatureSet getPrefixFeatures() {
		FeatureSet retVal = new FeatureSet();
		for(Diacritic dia:getPrefixDiacritics()) {
			retVal = FeatureSet.union(retVal, dia.getFeatureSet());
		}
		return retVal;
	}

	/**
	 * Get the primary glyph for this Phone.  All other
	 * parts of the Phone are 'attached' to this glyph.
	 * 
	 * @return the base character for the Phone
	 */
	public Character getBasePhone() {
		return basePhone;
	}

	/**
	 * <p>Set the base glyph for the Phone.  The base glyph must be 
	 * one of the following {@link IPATokenType}s:
	 * <ul>
	 * <li>{@link IPATokenType#CONSONANT}</li>
	 * <li>{@link IPATokenType#COVER_SYMBOL}</li>
	 * <li>{@link IPATokenType#GLIDE}</li>
	 * <li>{@link IPATokenType#VOWEL}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param basePhone
	 */
	public void setBasePhone(Character basePhone) {
		final IPATokenType tokenType = 
				IPATokens.getSharedInstance().getTokenType(basePhone);
		if(tokenType == null) {
			throw new IllegalArgumentException("Invalid glyph: '" + basePhone + "'");
		} else {
			if(tokenType != IPATokenType.CONSONANT
					&& tokenType != IPATokenType.COVER_SYMBOL
					&& tokenType != IPATokenType.GLIDE
					&& tokenType != IPATokenType.VOWEL) {
				throw new IllegalArgumentException("Base phones must be one of: CONSONANT, COVER_SYMBOL, GLIDE, VOWEL");
			}
		}
		this.basePhone = basePhone;
	}
	
	/**
	 * Get the string for the phone's base.
	 *
	 * @return the text for the phone's base 
	 */
	public String getBase() {
		final String retVal = "" + 
				(getBasePhone() == null ? "" : ""+getBasePhone());
		return retVal;
	}
	
	/**
	 * Get the feature set for the base phone
	 * 
	 * @return the base phone's feature set or an
	 *  empty set if not found
	 */
	public FeatureSet getBaseFeatures() {
		return getFeatures(getBasePhone());
	}

	/**
	 * <p>Get the combining diacritics for the phone.</p>
	 * 
	 * @return the combining diacritics, or an empty array
	 *  if no combining diacritics are available.
	 */
	public Diacritic[] getCombiningDiacritics() {
		return combiningDiacritics;
	}

	/**
	 * <p>Set the combining diacritics for this phone.  Each character
	 * must have a the {@link IPATokenType#COMBINING_DIACRITIC} token
	 * type.</p>
	 * 
	 * @param combiningDiacritics
	 * @throws IllegalArgumentException if one of the given diacritics
	 *  is not a combining diacritic
	 */
	public void setCombiningDiacritics(Diacritic[] combiningDiacritics) {
		for(Diacritic dc:combiningDiacritics) {
			if(dc.getType() != DiacriticType.COMBINING)
				throw new IllegalArgumentException();
		}
		this.combiningDiacritics = combiningDiacritics;
	}
	
	/**
	 * Get the tone diacritics for this phone.
	 * 
	 * @return tone diacritics
	 */
	public Diacritic[] getToneDiacritics() {
		final List<Diacritic> retVal = new ArrayList<Diacritic>();
		for(Diacritic dia:getSuffixDiacritics()) {
			if(dia.getType() == DiacriticType.TONE) {
				retVal.add(dia);
			}
		}
		return retVal.toArray(new Diacritic[0]);
	}
	
	public Diacritic[] getLengthDiacritics() {
		final List<Diacritic> retVal = new ArrayList<Diacritic>();
		for(Diacritic dia:getSuffixDiacritics()) {
			if(dia.getType() == DiacriticType.LENGTH) {
				retVal.add(dia);
			}
		}
		return retVal.toArray(new Diacritic[0]);
	}
	
	/**
	 * Get the string for the combining diacritic portion of the 
	 * phone.
	 * 
	 * @return the combining diacritic string
	 */
	public String getCombining() {
		final StringBuilder sb = new StringBuilder();
		for(Diacritic dia:getCombiningDiacritics()) {
			sb.append(dia.getText());
		}
		return sb.toString();
	}
	
	/**
	 * Get the feature set for all combining
	 * diacritics.
	 * 
	 * @return the feature set for all combining
	 *  diacritics
	 */
	public FeatureSet getCombiningFeatures() {
		FeatureSet retVal = new FeatureSet();
		for(Diacritic dia:getCombiningDiacritics()) {
			retVal = FeatureSet.union(retVal, dia.getFeatureSet());
		}
		return retVal;
	}
	
	/* Get/Set methods */
	public Diacritic[] getSuffixDiacritics() {
		return suffixDiacritics;
	}

	/**
	 * Set the prefix diacritics for this Phone.
	 * 
	 * @param prefixDiacritics
	 */
	public void setSuffixDiacritics(Diacritic[] suffixDiacritics) {
		this.suffixDiacritics = suffixDiacritics;
	}
	
	/**
	 * Get the string for this phone's suffix.
	 * 
	 * @return the text for the suffix portion of
	 *  the Phone
	 */
	public String getSuffix() {
		final StringBuilder sb = new StringBuilder();
		for(Diacritic dia:getSuffixDiacritics()) {
			sb.append(dia.getText());
		}
		return sb.toString();
	}
	
	/**
	 * Get the feature set for the suffix diacritic
	 * 
	 * @return feature set for the suffix diacritic 
	 *  or an empty set if not found
	 */
	public FeatureSet getSuffixFeatures() {
		FeatureSet retVal = new FeatureSet();
		for(Diacritic dia:getSuffixDiacritics()) {
			retVal = FeatureSet.union(retVal, dia.getFeatureSet());
		}
		return retVal;
	}
	
	private FeatureSet getFeatures(Character c) {
		FeatureSet retVal = new FeatureSet();
		if(c != null) {
			FeatureSet fs = FeatureMatrix.getInstance().getFeatureSet(c);
			if(fs != null) {
				retVal = FeatureSet.union(retVal, fs);
			}
		}
		return retVal;
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		FeatureSet retVal = new FeatureSet();
		retVal = FeatureSet.union(retVal, getPrefixFeatures());
		retVal = FeatureSet.union(retVal, getBaseFeatures());
		retVal = FeatureSet.union(retVal, getCombiningFeatures());
		retVal = FeatureSet.union(retVal, getSuffixFeatures());
		return retVal;
	}

	@Override
	public String getText() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getPrefix());
		sb.append(getBasePhone());
		sb.append(getCombining());
		sb.append(getSuffix());
		return sb.toString();
	}
}

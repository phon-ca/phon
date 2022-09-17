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

import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;

/**
 * A special type of IPAElement which represents a
 * reference to a phonex group.  This is used during
 * phonex replacement only.
 *
 */
public class PhonexMatcherReference extends IPAElement implements PrefixDiacritics, SuffixDiacritics, CombiningDiacritics {
	
	private Integer groupIndex;
	
	private String groupName;
	
	private Diacritic[] prefixDiacritics = new Diacritic[0];
	
	private Diacritic[] suffixDiacritics = new Diacritic[0];
	
	private Diacritic[] combiningDiacritics = new Diacritic[0];
	
	public PhonexMatcherReference(Integer groupIndex) {
		this.groupIndex = groupIndex;
	}
	
	public PhonexMatcherReference(String groupName) {
		this.groupName = groupName;
	}
	
	public int getGroupIndex() {
		return (groupIndex == null ? -1 : groupIndex);
	}
	
	public String getGroupName() {
		return groupName;
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

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		var image = "\\" +
				(groupName != null ? "{" + groupName + "}" : groupIndex);
		final StringBuilder sb = new StringBuilder();
		sb.append(getPrefix());
		sb.append(image);
		sb.append(getCombining());
		sb.append(getSuffix());
		return sb.toString();
	}

}

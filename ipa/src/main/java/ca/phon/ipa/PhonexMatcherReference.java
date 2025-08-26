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

/**
 * A special type of IPAElement which represents a
 * reference to a phonex group.  This is used during
 * phonex replacement only.
 *
 */
public class PhonexMatcherReference extends IPAElement implements PrefixDiacritics, SuffixDiacritics, CombiningDiacritics {
	
	private final Integer groupIndex;
	
	private final String groupName;
	
	private final Diacritic[] prefixDiacritics;
	
	private final Diacritic[] suffixDiacritics;
	
	private final Diacritic[] combiningDiacritics;
	
	public PhonexMatcherReference(Integer groupIndex) {
		this(null, groupIndex, null, null, null);
	}
	
	public PhonexMatcherReference(String groupName) {
		this(groupName, null, null, null, null);
	}

    public PhonexMatcherReference(String groupName, Integer groupIndex, Diacritic[] prefixDiacritics, Diacritic[] combiningDiacritics, Diacritic[] suffixDiacritics) {
        super(null, null);
        this.groupName = groupName;
        this.groupIndex = groupIndex;
        this.prefixDiacritics = (prefixDiacritics == null ? new Diacritic[0] : prefixDiacritics);
        this.combiningDiacritics = (combiningDiacritics == null ? new Diacritic[0] : combiningDiacritics);
        this.suffixDiacritics = (suffixDiacritics == null ? new Diacritic[0] : suffixDiacritics);
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
	 * Get the string representing this phone's prefix.
	 * 
	 * @return the prefix string
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
        String sb = getPrefix() +
                image +
                getCombining() +
                getSuffix();
		return sb;
	}

}

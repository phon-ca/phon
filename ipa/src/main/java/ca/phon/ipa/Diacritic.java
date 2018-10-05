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

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;
import ca.phon.ipa.parser.IPATokenType;
import ca.phon.ipa.parser.IPATokens;

/**
 * Diacritics are attached to phones as either prefix, suffix
 * or combining diacritics.
 *
 */
public class Diacritic extends IPAElement {
	
	/**
	 * The diacritic character
	 */
	private Character character;
	
	private Diacritic[] prefixDiacritics = new Diacritic[0];
	
	private Diacritic[] suffixDiacritics = new Diacritic[0];
	
	/**
	 * Constructor
	 * 
	 * @param ch
	 */
	Diacritic(Character ch) {
		super();
		setCharacter(ch);
	}
	
	Diacritic(Diacritic[] prefix, Character ch, Diacritic[] suffix) {
		super();
		setCharacter(ch);
		setPrefixDiacritics(prefix);
		setSuffixDiacritics(suffix);
	}
	
	public void setCharacter(Character ch) {
		this.character = ch;
	}
	
	public Character getCharacter() {
		return this.character;
	}
	
	public Diacritic[] getPrefixDiacritics() {
		return prefixDiacritics;
	}
	
	public void setPrefixDiacritics(Diacritic[] prefixDiacritics) {
		this.prefixDiacritics = prefixDiacritics;
	}
	
	public Diacritic[] getSuffixDiacritics() {
		return suffixDiacritics;
	}

	public void setSuffixDiacritics(Diacritic[] suffixDiacritics) {
		this.suffixDiacritics = suffixDiacritics;
	}
	
	@Override
	protected FeatureSet _getFeatureSet() {
		FeatureSet fs = getFeatures(character);
		for(Diacritic dia:prefixDiacritics) {
			fs = FeatureSet.union(fs, dia.getFeatureSet());
		}
		for(Diacritic dia:suffixDiacritics) {
			fs = FeatureSet.union(fs, dia.getFeatureSet());
		}
		return fs;
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
	
	public DiacriticType getType() {
		final IPATokens tokens = IPATokens.getSharedInstance();
		final IPATokenType tt = tokens.getTokenType(character);
		
		DiacriticType retVal = null;
		
		if(tt == IPATokenType.PREFIX_DIACRITIC) {
			retVal = DiacriticType.PREFIX;
		} else if(tt == IPATokenType.SUFFIX_DIACRITIC) {
			retVal = DiacriticType.SUFFIX;
		} else if(tt == IPATokenType.TONE) {
			retVal = DiacriticType.TONE;
		} else if(tt == IPATokenType.COMBINING_DIACRITIC) {
			retVal = DiacriticType.COMBINING;
		} else if(tt == IPATokenType.LONG || tt == IPATokenType.HALF_LONG) {
			retVal = DiacriticType.LENGTH;
		}
		
		return retVal;
	}

	@Override
	public String getText() {
		final StringBuilder sb = new StringBuilder();
		for(Diacritic dia:prefixDiacritics) {
			sb.append(dia.getText());
		}
		sb.append(character);
		for(Diacritic dia:suffixDiacritics) {
			sb.append(dia.getText());
		}
		return sb.toString();
	}

}

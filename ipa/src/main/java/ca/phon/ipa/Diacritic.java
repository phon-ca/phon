/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
		final FeatureSet fs = getFeatures(character);
		for(Diacritic dia:prefixDiacritics) {
			fs.union(dia.getFeatureSet());
		}
		for(Diacritic dia:suffixDiacritics) {
			fs.union(dia.getFeatureSet());
		}
		return fs;
	}
	
	private FeatureSet getFeatures(Character c) {
		final FeatureSet retVal = new FeatureSet();
		if(c != null) {
			FeatureSet fs = FeatureMatrix.getInstance().getFeatureSet(c);
			if(fs != null) {
				retVal.union(fs);
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

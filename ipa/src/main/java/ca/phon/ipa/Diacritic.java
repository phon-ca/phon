package ca.phon.ipa;

import ca.phon.ipa.features.FeatureMatrix;
import ca.phon.ipa.features.FeatureSet;

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
	
	/**
	 * Constructor
	 * 
	 * @param ch
	 */
	Diacritic(Character ch) {
		super();
		setCharacter(ch);
	}
	
	public void setCharacter(Character ch) {
		
		this.character = ch;
		
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return getFeatures(character);
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

	@Override
	public String getText() {
		return character.toString();
	}

}

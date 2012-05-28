package ca.phon.ipa.phone;

import java.util.Set;

import ca.phon.ipa.IPATokenType;
import ca.phon.ipa.IPATokens;
import ca.phon.ipa.featureset.FeatureMatrix;
import ca.phon.ipa.featureset.FeatureSet;

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
public final class BasicPhone extends Phone {
	/**
	 * Prefix diacritic
	 */
	private Character prefixDiacritic;
	
	/**
	 * Base
	 */
	private Character basePhone;
	
	/**
	 * Combining diacritics
	 */
	private Character[] combiningDiacritics = new Character[0];
	
	/**
	 * Length
	 */
	private float length = 0;
	
	/**
	 * Suffix diacritic
	 */
	private Character suffixDiacritic;
	
	/**
	 * Create a new Phone for the given base
	 * 
	 * @param basePhone
	 */
	BasicPhone(Character basePhone) {
		super();
		setBasePhone(basePhone);
	}
	
	/**
	 * Full constructor
	 * 
	 * @param prefixDiacritic
	 * @param basePhone
	 * @param combiningDiacritics
	 * @param length
	 * @param suffixDiacritic
	 */
	BasicPhone(Character prefixDiacritic, Character basePhone,
			Character[] combiningDiacritics, float length,
			Character suffixDiacritic) {
		super();
		setPrefixDiacritic(prefixDiacritic);
		setBasePhone(basePhone);
		setCombiningDiacritics(combiningDiacritics);
		setLength(length);
		setSuffixDiacritic(suffixDiacritic);
	}

	/* Get/Set methods */
	public Character getPrefixDiacritic() {
		return prefixDiacritic;
	}

	/**
	 * Set the prefix diacritic for this Phone.
	 * 
	 * @param prefixDiacritic
	 * @throws IllegalArgumentException if the given character
	 *  does not have the PREFIX_DIACRITIC or SUFFIX_DIACRITIC
	 *  token type as specified by {@link IPATokens#getTokenType(Character)}
	 */
	public void setPrefixDiacritic(Character prefixDiacritic) {
		final IPATokenType tokenType = 
				IPATokens.getSharedInstance().getTokenType(prefixDiacritic);
		if(tokenType == null
				|| (tokenType != IPATokenType.PREFIX_DIACRITIC && tokenType != IPATokenType.SUFFIX_DIACRITIC)) {
			throw new IllegalArgumentException("Prefix diacritic must be a space-modifiying glyph.");
		}
		this.prefixDiacritic = prefixDiacritic;
	}
	
	/**
	 * Get the string representing this phone's prefix.
	 * 
	 * @return
	 */
	public String getPrefix() {
		String retVal = "";
		final Character prefixChar = getPrefixDiacritic();
		if(prefixChar != null) {
			retVal += prefixChar;
			IPATokenType type = IPATokens.getSharedInstance().getTokenType(prefixChar);
			if(type == IPATokenType.SUFFIX_DIACRITIC)
				retVal += '\u0335';
		}
		return retVal;
	}
	
	/**
	 * Get the feature set for the prefix diacritic.
	 * 
	 * @return feature set for the prefix diacritic or
	 *  an empty set if not found
	 */
	public FeatureSet getPrefixFeatures() {
		return getFeatures(getPrefixDiacritic());
	}

	/**
	 * Get the primary glyph for this Phone.  All other
	 * parts of the Phone are 'attached' to this glyph.
	 * 
	 * @return the base caracter for the Phone
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
	public Character[] getCombiningDiacritics() {
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
	public void setCombiningDiacritics(Character[] combiningDiacritics) {
		for(Character dc:combiningDiacritics) {
			final IPATokenType tt = IPATokens.getSharedInstance().getTokenType(dc);
			if(tt != IPATokenType.COMBINING_DIACRITIC)
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
		String retVal = "";
		
		for(Character c:getCombiningDiacritics())
			retVal += ""+c;
		
		return retVal;
	}
	
	/**
	 * Get the feature set for all combining
	 * diacritics.
	 * 
	 * @return the feature set for all combining
	 *  diacritics
	 */
	public FeatureSet getCombiningFeatures() {
		final FeatureSet retVal = new FeatureSet();
		
		for(Character c:getCombiningDiacritics()) {
			retVal.union(getFeatures(c));
		}
		
		return retVal;
	}

	/**
	 * Get the length of the phone as an integer value.
	 * Length is measured between [0-3], with a step of
	 * 0.5.
	 * 
	 * @return the length of the phone as an integer
	 */
	public float getLength() {
		return length;
	}

	/**
	 * Set the length of the phone.  Given length
	 * must be a value between 0 and 3.
	 * 
	 * @param length
	 * @throws IllegalArgumentException if the given length
	 *  is not between 0 and 3 and/or not a multiple of 0.5.
	 */
	public void setLength(float length) {
		if(length < 0 || length > 3 || ((length * 2.0) % 2 != 0)) {
			throw new IllegalArgumentException("Phone length must be one of: 0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0");
		}
		this.length = length;
	}
	
	/**
	 * Get the phone length as a string.
	 * 
	 * @return the text representation of the phone's
	 *  transcribed length
	 */
	public String getLengthString() {
		String retVal = "";
		
		if(getLength() > 0) {
			Set<Character> longChars = 
					IPATokens.getSharedInstance().getCharactersForType(IPATokenType.LONG);
			Character longChar = (longChars.size() > 0 ? longChars.iterator().next() : '\u02d0');
			Set<Character> hlChars =
					IPATokens.getSharedInstance().getCharactersForType(IPATokenType.HALF_LONG);
			Character hlChar = (hlChars.size() > 0 ? hlChars.iterator().next() : '\u02d1');
			
			int numLongs = (int)getLength();
			boolean hasHalf = (getLength() - numLongs) > 0;
			
			for(int i = 0; i < numLongs; i++) retVal += longChar + "";
			if(hasHalf) retVal += hlChar + "";
		}
		
		return retVal;
	}
	

	public Character getSuffixDiacritic() {
		return suffixDiacritic;
	}

	/**
	 * Set the suffix diacritic for this Phone.
	 * 
	 * @param suffixDiacritic
	 * @throws IllegalArgumentException if the given character
	 *  does not have the PREFIX_DIACRITIC or SUFFIX_DIACRITIC
	 *  token type as specified by {@link IPATokens#getTokenType(Character)} 
	 */
	public void setSuffixDiacritic(Character suffixDiacritic) {
		final IPATokenType tokenType = 
				IPATokens.getSharedInstance().getTokenType(suffixDiacritic);
		if(tokenType == null
				|| (tokenType != IPATokenType.PREFIX_DIACRITIC && tokenType != IPATokenType.SUFFIX_DIACRITIC)) {
			throw new IllegalArgumentException("Suffix diacritic must be a space-modifiying glyph.");
		}
		this.suffixDiacritic = suffixDiacritic;
	}
	
	/**
	 * Get the string for this phone's suffix.
	 * 
	 * @return the text for the suffix portion of
	 *  the Phone
	 */
	public String getSuffix() {
		String retVal = "";
		Character suffixChar = getSuffixDiacritic();
		if(suffixChar != null) {
			retVal += suffixChar;
			IPATokenType type = IPATokens.getSharedInstance().getTokenType(suffixChar);
			if(type == IPATokenType.PREFIX_DIACRITIC)
				retVal += '\u0335';
		}
		return retVal;
	}
	
	/**
	 * Get the feature set for the suffix diacritic
	 * 
	 * @return feature set for the suffix diacritic 
	 *  or an empty set if not found
	 */
	public FeatureSet getSuffixFeatures() {
		return getFeatures(getSuffixDiacritic());
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
	protected FeatureSet _getFeatureSet() {
		final FeatureSet retVal = new FeatureSet();
			retVal.union(getPrefixFeatures());
			retVal.union(getBaseFeatures());
			retVal.union(getCombiningFeatures());
			retVal.union(getSuffixFeatures());
		return retVal;
	}

	@Override
	public String getText() {
		final String retVal =
				getPrefix() +
				getBase() +
				getCombining() +
				getLengthString() +
				getSuffix();
		return retVal;
	}
}

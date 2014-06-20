package ca.phon.ipa.parser;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.xml.TokenType;

/**
 * Every IPA glyph must have an associated token type.
 * 
 * Token types are used during {@link IPAElement} parsing
 * and are available via {@link IPATokens#getTokenType(Character)}
 */
public enum IPATokenType {
	CONSONANT("Consonant"),
    CLOSE_PAREN("Close Paren"),
    COMBINING_DIACRITIC("Combining Diacritic"),
    COVER_SYMBOL("Cover Symbol"),
    GLIDE("Glide"),
    HALF_LONG("Half-long"),
    LIGATURE("Ligature"),
    LONG("Long"),
    MAJOR_GROUP("Major Intonation Group"),
    MINOR_GROUP("Minor Intonation Group"),
    OPEN_PAREN("Open Paren"),
    PERIOD("Period"),
    PLUS("Plus"),
    PREFIX_DIACRITIC("Prefix Diacritic"),
    PRIMARY_STRESS("Primary Stress"),
    ROLE_REVERSAL("Role Reversal"),
    SECONDARY_STRESS("Secondary Stress"),
    SPACE("Space"),
    SUFFIX_DIACRITIC("Suffix Diacritic"),
    TONE("Tone"),
    VOWEL("Vowel"),
    COLON("Colon"),
    SCTYPE("Syllable ConstituentType"),
    SANDHI("Sandhi"),
    DOLLAR_SIGN("Dollar sign"),
    OPEN_BRACE("Open brace"),
    GROUP_NAME("Group name"),
    CLOSE_BRACE("Close brace"),
    DIGIT("Digit"),
    INTRA_WORD_PAUSE("Intra-word pause");
	
	String tokenName = "Unknown";
	
	private IPATokenType(String name) {
		this.tokenName = name;
	}
	
	public String getName() {
		return this.tokenName;
	}

	public static IPATokenType fromXMLType(TokenType type) {
		IPATokenType retVal = null;
		
		for(IPATokenType t:IPATokenType.values()) {
			if(type.toString().equalsIgnoreCase(t.toString())) {
				retVal = t;
				break;
			}
		}
		
		return retVal;
	}
	
	/**
	 * Return ipa token type from given string.
	 * 
	 * @param name either token name as returned by {@link #toString()} or
	 *  by {@link #getName()}
	 */
	public static IPATokenType fromString(String name) {
		for(IPATokenType tt:values()) {
			if(tt.getName().equals(name) || tt.toString().equals(name)) {
				return tt;
			}
		}
		return null;
	}
}

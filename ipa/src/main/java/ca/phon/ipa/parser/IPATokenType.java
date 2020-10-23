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
package ca.phon.ipa.parser;

import ca.phon.ipa.*;
import ca.phon.ipa.xml.*;

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
    TONE_NUMBER("Tone Number"),
    VOWEL("Vowel"),
    COLON("Colon"),
    SCTYPE("Syllable ConstituentType"),
    SANDHI("Sandhi"),
    DOLLAR_SIGN("Dollar sign"),
    OPEN_BRACE("Open brace"),
    GROUP_NAME("Group name"),
    CLOSE_BRACE("Close brace"),
    DIGIT("Digit"),
    INTRA_WORD_PAUSE("Intra-word pause"),
    ALIGNMENT("Alignment");
	
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

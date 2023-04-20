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

package ca.phon.orthography;

/**
 * Word suffix codes.  Suffixes are applied to words
 * after a '@' character.
 */
public enum WordFormType {
	// w attribute:separated-prefix
	SEPARATED_PREFIX("#", "separated-prefix"),
	// w attribute:user-special-form
	USER_SPECIAL_FORM("@z", "user-special-form"),
	// w attribute:formType
	ADDITION("@a", "addition"),
	BABBLING("@b", "babbling"),
	CHILD_INVENTED("@c", "child-invented"),
	DIALECT("@d", "dialect"),
	ECHOLALIA("@e", "echolalia"),
	FAMILY_SPECIFIC("@f", "family-specific"),
	FILLED_PAUSE("@fp", "filled pause"),
	@Deprecated
	FILLER_SYLLABLE("@fs", "filler syllable"),
	GENERIC("@g", "generic"),
	INTERJECTION("@i", "interjection"),
	KANA("@k", "kana"),
	LETTER("@l", "letter"),
	NEOLOGISM("@n", "neologism"),
	NO_VOICE("@nv", "no voice"),
	ONOMATOPOEIA("@o", "onomatopoeia"),
	PHONOLOGY_CONSISTENT("@p", "phonology consistent"),
	QUOTED_METAREFERENCE("@q", "quoted metareference"),
	SIGN_SPEECH("@sas", "sign speech"),
	SINGING("@si", "singing"),
	SIGNED_LANGUAGE("@sl", "signed language"),
	TEST("@t", "test"),
	UNIBET("@u", "UNIBET"),
	WORDS_TO_BE_EXCLUDED("@x", "words to be excluded"),
	WORD_PLAY("@wp", "word play")
	;
	
	private String code;
	
	private String displayName;
	
	private WordFormType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}
	
	public static WordFormType fromCode(String code) {
		WordFormType retVal = null;
		
		for(WordFormType v:values()) {
			if(v.getCode().equals(code)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

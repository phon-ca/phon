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

import ca.phon.util.Documentation;

/**
 * Word suffix codes.  Suffixes are applied to words
 * after a '@' character.
 */
public enum WordFormType {
	@Documentation("https://talkbank.org/manuals/CHAT.html#Addition_Marker")
	ADDITION("@a", "addition"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Babbling_Marker")
	BABBLING("@b", "babbling"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#ChildInvented_Marker")
	CHILD_INVENTED("@c", "child-invented"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#DialectForm_Marker")
	DIALECT("@d", "dialect"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#EcholaliaForm_Marker")
	ECHOLALIA("@e", "echolalia"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#FamilySpecificForm_Marker")
	FAMILY_SPECIFIC("@f", "family-specific"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#FilledPause_Marker")
	FILLED_PAUSE("@fp", "filled pause"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#GeneralSpecialForm_Marker")
	GENERIC("@g", "generic"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Interjection_Marker")
	INTERJECTION("@i", "interjection"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Kana_Marker")
	KANA("@k", "kana"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Letter_Marker")
	LETTER("@l", "letter"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Neologism_Marker")
	NEOLOGISM("@n", "neologism"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#NonvoicedForm_Marker")
	NO_VOICE("@nv", "no voice"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Onomatopoeia_Marker")
	ONOMATOPOEIA("@o", "onomatopoeia"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#PCF_Marker")
	PHONOLOGY_CONSISTENT("@p", "phonology consistent"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#MetalinguisticReference_Marker")
	QUOTED_METAREFERENCE("@q", "quoted metareference"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#SignAndSpeech_Marker")
	SIGN_SPEECH("@sas", "sign speech"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Singing_Marker")
	SINGING("@si", "singing"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#SignLanguage_Marker")
	SIGNED_LANGUAGE("@sl", "signed language"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#TestWord_Marker")
	TEST("@t", "test"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Unibet_Marker")
	UNIBET("@u", "UNIBET"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#Excluded_Marker")
	WORDS_TO_BE_EXCLUDED("@x", "words to be excluded"),
	@Documentation("https://talkbank.org/manuals/CHAT.html#WordPlay_Marker")
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

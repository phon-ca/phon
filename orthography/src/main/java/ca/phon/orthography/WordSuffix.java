/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

package ca.phon.orthography;

/**
 * Word suffix codes.  Suffixes are applied to words
 * after a '@' character.
 */
public enum WordSuffix {
	BABBLING("b", "babbling"),
	CHILD_INVENTED("c", "child-invented"),
	DIALECT("d", "dialect"),
	FAMILY_SPECIFIC("f", "family-specific"),
	FILLED_PAUSE("fp", "filled-pause"),
	FILLER_SYLLABLE("fs", "filler syllable"),
	GENERIC("g", "generic"),
	INTERJECTION("i", "interjection"),
	KANA("k", "kana"),
	LETTER("l", "letter"),
	NEOLOGISM("n", "neologism"),
	NO_VOICE("nv", "no voice"),
	ONOMATOPOEIA("o", "onomatopoeia"),
	PHONOLOGY_CONSISTENT("p", "phonology consistent"),
	PROTO_MORPHEME("pm", "proto-morpheme"),
	QUOTED_METAREFERENCE("q", "quoted metareference"),
	SIGN_SPEECH("sas", "sign speech"),
	SIGNING("si", "signing"),
	TEST("t", "test"),
	UNIBET("u", "UNIBET"),
	WORDS_TO_BE_EXCLUDED("x", "words to be excluded"),
	WORD_PLAY("wp", "word play");
	
	private String code;
	
	private String displayName;
	
	private WordSuffix(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}

	public String getDisplayName() {
		return this.displayName;
	}
	
	public static WordSuffix fromCode(String code) {
		WordSuffix retVal = null;
		
		for(WordSuffix v:values()) {
			if(v.getCode().equals(code)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

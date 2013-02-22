/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
	BABBLING,
	CHILD_INVENTED,
	DIALECT,
	FAMILY_SPECIFIC,
	FILLED_PAUSE,
	FILLER_SYLLABLE,
	GENERIC,
	INTERJECTION,
	KANA,
	LETTER,
	NEOLOGISM,
	NO_VOICE,
	ONOMATOPOEIA,
	PHONOLOGY_CONSISTENT,
	PROTO_MORPHEME,
	QUOTED_METAREFERENCE,
	SIGN_SPEECH,
	SIGNING,
	TEST,
	UNIBET,
	WORDS_TO_BE_EXCLUDED,
	WORD_PLAY;

	/** Codes */
	private String[] codes = {
		"b",
		"c",
		"d",
		"f",
		"fp",
		"fs",
		"g",
		"i",
		"k",
		"l",
		"n",
		"nv",
		"o",
		"p",
		"pm",
		"q",
		"sas",
		"si",
		"t",
		"u",
		"x",
		"wp"
	};

	public String getCode() {
		return codes[ordinal()];
	}

	/**
	 * Display names
	 */
	private String[] displayNames = {
		"babbling",
		"child-invented",
		"dialect",
		"family-specific",
		"filled-pause",
		"filler syllable",
		"generic",
		"interjection",
		"kana",
		"letter",
		"neologism",
		"no voice",
		"onomatopoeia",
		"phonology consistent",
		"proto-morpheme",
		"quoted metareference",
		"sign speech",
		"signing",
		"test",
		"UNIBET",
		"words to be excluded",
		"word play"
	};

	public String getDisplayName() {
		return displayNames[ordinal()];
	}
}

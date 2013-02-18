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

package ca.phon.session.orthoparser;

/**
 * Codes for word prefix.
 */
public enum WordPrefixCodes {
	OMISSION,
	ELLIPSIS,
	FRAGMENT,
	UNINTELLIGIBLE_WORD,
	UNINTELLIGIBLE,
	UNINTELLIGIBLE_WORD_WITH_PHO,
	UNTRANSCRIBED;

	/**
	 * Codes
	 */
	private String[] prefixCodes = {
		"0",
		"00",
		"&",
		"xx",
		"xxx",
		"yy",
		"www"
	};

	public String getCode() {
		return prefixCodes[ordinal()];
	}

	/**
	 * Display names
	 */
	private String[] displayNames = {
		"omission",
		"ellipsis",
		"fragment",
		"unintelligible-word",
		"unintelligible",
		"unintelligible-word-with-pho",
		"untranscribed"
	};

	public String getDisplayName() {
		return displayNames[ordinal()];
	}

}

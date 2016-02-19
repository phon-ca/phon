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

package ca.phon.orthography;

/**
 * Codes for word prefix.
 */
public enum WordPrefixType {
	// w attribute:type
	OMISSION("0", "omission"),
	FRAGMENT("&", "fragment"),
	// w attribute:untranscribed
	UNINTELLIGIBLE("xxx", "unintelligible"),
	UNINTELLIGIBLE_WORD_WITH_PHO("yyy", "unintelligible-word-with-pho"),
	UNTRANSCRIBED("www", "untranscribed");
	
	private String code;
	
	private String displayName;
	
	private WordPrefixType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}

	public static WordPrefixType fromCode(String code) {
		WordPrefixType retVal = null;
		
		for(WordPrefixType v:values()) {
			if(v.getCode().equals(code)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

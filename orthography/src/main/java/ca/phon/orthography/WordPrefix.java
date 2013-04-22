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
 * Codes for word prefix.
 */
public enum WordPrefix {
	ELLIPSIS("00", "ellipsis"),
	OMISSION("0", "omission"),
	FRAGMENT("&", "fragment"),
	UNINTELLIGIBLE("xxx", "unintelligible"),
	UNINTELLIGIBLE_WORD("xx", "unintelligible-word"),
	UNINTELLIGIBLE_WORD_WITH_PHO("yyy", "unintelligible-word-with-pho"),
	UNTRANSCRIBED("www", "untranscribed");
	
	private String code;
	
	private String displayName;
	
	private WordPrefix(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return this.code;
	}
	
	public String getDisplayName() {
		return this.displayName;
	}

	public static WordPrefix fromCode(String code) {
		WordPrefix retVal = null;
		
		for(WordPrefix v:values()) {
			if(v.getCode().equals(code)) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
}

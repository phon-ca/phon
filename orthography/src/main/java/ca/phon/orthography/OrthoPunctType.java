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
 * Punctuation support in orthography.
 */
public enum OrthoPunctType {
	PERIOD('.'),
	COMMA(','),
	EXCLAMATION('!'),
	QUESTION('?'),
	AT('@'),
	HASH('#'),
	DOLLARSIGN('$'),
	PERCENT('%'),
	CARET('^'),
	AMPERSTAND('&'),
	OPEN_BRACE('{'),
	CLOSE_BRACE('}'),
	FORWARD_SLASH('/'),
	BACK_SLASH('\\');
	
	private final char punctChar;
	
	private OrthoPunctType(char c) {
		this.punctChar = c;
	}
	
	public char getChar() {
		return this.punctChar;
	}
	
	public static OrthoPunctType fromChar(char c) {
		OrthoPunctType retVal = null;
		
		for(OrthoPunctType v:values()) {
			if(v.getChar() == c) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
	
}

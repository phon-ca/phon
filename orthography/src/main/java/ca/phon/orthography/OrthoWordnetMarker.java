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
 * Symbols used to create wordnets.
 */
public enum OrthoWordnetMarker {
	COMPOUND('+'),
	CLITIC('~');
	
	private char marker;
	
	private OrthoWordnetMarker(char c) {
		this.marker = c;
	}
	
	public char getMarker() {
		return this.marker;
	}
	
	public static OrthoWordnetMarker fromMarker(char c) {
		OrthoWordnetMarker retVal = null;
		
		for(OrthoWordnetMarker v:values()) {
			if(v.getMarker() == c) {
				retVal = v;
				break;
			}
		}
		
		return retVal;
	}
	
	@Override
	public String toString() {
		return "" + getMarker();
	}

}

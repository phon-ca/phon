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
package ca.phon.phonex;

/**
 * Quantifier types. 
 *
 */
public enum QuantifierType {
	ZERO_OR_MORE("*"),
	ZERO_OR_ONE("?"),
	ONE_OR_MORE("+"),
	BOUNDED("<x,y>");
	
	private String value;
	
	private QuantifierType(String v) {
		this.value = v;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return getValue();
	}
	
	public static QuantifierType fromString(String txt) {
		QuantifierType retVal = null;
		for(QuantifierType q:QuantifierType.values()) {
			if(q.getValue().equals(txt)) {
				retVal = q;
				break;
			}
		}
		return retVal;
	}
	
}

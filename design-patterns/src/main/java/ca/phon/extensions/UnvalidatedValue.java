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
package ca.phon.extensions;

import java.text.ParseException;

/**
 * <p>A container for unvalidated data.  This class
 * may be attached to a tier which the data
 * entered by the user or data read from disk
 * is not parse-able.</p>
 * 
 */
public class UnvalidatedValue {
	
	private String unvalidatedData;
	
	private ParseException parseErr;
	
	public UnvalidatedValue() {
		super();
	}
	
	public UnvalidatedValue(String value) {
		super();
		this.unvalidatedData = value;
	}
	
	public UnvalidatedValue(String value, ParseException pe) {
		super();
		this.unvalidatedData = value;
		this.parseErr = pe;
	}
	
	public String getValue() {
		return this.unvalidatedData;
	}
	
	public void setValue(String value) {
		this.unvalidatedData = value;
	}
	
	public ParseException getParseError() {
		return this.parseErr;
	}
	
	public void setParseError(ParseException pe) {
		this.parseErr = pe;
	}

}

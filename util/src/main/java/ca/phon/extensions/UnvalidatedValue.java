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
package ca.phon.extensions;

import java.text.*;

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

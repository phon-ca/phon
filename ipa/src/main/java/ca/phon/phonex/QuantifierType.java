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

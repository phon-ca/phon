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
package ca.phon.orthography;

/**
 * Symbols used to create wordnets.
 */
public enum CompoundWordMarkerType {
	COMPOUND('+'),
	CLITIC('~');
	
	private char marker;
	
	private CompoundWordMarkerType(char c) {
		this.marker = c;
	}
	
	public char getMarker() {
		return this.marker;
	}
	
	public static CompoundWordMarkerType fromMarker(char c) {
		CompoundWordMarkerType retVal = null;
		
		for(CompoundWordMarkerType v:values()) {
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

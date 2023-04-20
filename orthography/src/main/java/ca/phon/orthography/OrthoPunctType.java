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
 * Punctuation support in orthography.
 */
public enum OrthoPunctType {
	PERIOD('.'),
	COLON(':'),
	SEMICOLON(';'),
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
	BACK_SLASH('\\'),

	/* CHAT ca-element-type */
	/*
	BLOCKED_SEGMENTS('\u2260'),
	CONSTRICTION('\u223e'),
	INHALATION('\u2219'),
	LAUGH_IN_WORD('\u1f29'),
	PITCH_DOWN('\u2193'),
	PITCH_RESET('\u21bb'),
	PITCH_UP('\u2191'),
	PRIMARY_STRESS('\u02c8'),
	SECONDARY_STRESS('\u02cc');
	 */
	;
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

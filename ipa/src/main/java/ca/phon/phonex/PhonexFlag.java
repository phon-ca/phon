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

import java.util.*;

/**
 * Phonex flags
 */
public enum PhonexFlag {
	ALLOW_OVERLAPPING_MATCHES('o', 0x01)
	/* currently not implemented
	STRIP_DIACRITICS('d', 0x02),
	STRIP_PUNCTUATION('p', 0x04)
	*/
	;

	private char flagChar;

	private int bitmask;

	public static List<PhonexFlag> getFlags(int flags) {
		List<PhonexFlag> retVal = new ArrayList<>();
		for(PhonexFlag flag:values()) {
			if(flag.checkFlag(flags))
				retVal.add(flag);
		}
		return Collections.unmodifiableList(retVal);
	}

	public static PhonexFlag fromChar(char flagChar) {
		PhonexFlag retVal = null;
		for(PhonexFlag flag:values()) {
			if(flag.getFlagChar() == flagChar) {
				retVal = flag;
				break;
			}
		}
		return retVal;
	}

	private PhonexFlag(char flagChar, int bitmask) {
		this.flagChar = flagChar;
		this.bitmask = bitmask;
	}

	public int getBitmask() {
		return this.bitmask;
	}

	public char getFlagChar() {
		return this.flagChar;
	}

	public boolean checkFlag(int flags) {
		return ((flags & getBitmask()) == getBitmask());
	}

}

/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.util;

public class PhonConstants {
	
	/** A right-facing arrow */
	public static final char rightArrow = 0x2192;

	/** Double arrow */
	public static final char doubleArrow = 0x2194;
	
	/** '...' */
	public static final char ellipsis = 0x2026;
	
	/** 'null' */
	public static final char nullChar = 0x2205;
	
	/** Illegal filname characters */
	public static final char[] illegalFilenameChars = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '#' };

}

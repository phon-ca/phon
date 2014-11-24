/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.util;

public class PhonConstants {
	
	/** A right-facing arrow */
	public static final char rightArrow = 0x2192;

	/** Double arrow */
	public static final char doubleArrow = 0x2194;
	
	/** '...' */
	public static final char ellipsis = 0x2026;
	
	/** 'null' */
	public static final char nullChar = 0x00d8;
	
	/** Illegal filname characters */
	public static final char[] illegalFilenameChars = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };

}

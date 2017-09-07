/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.util;

import java.text.*;


/**
 * Format time values in miliseconds into parseable strings.
 */
public class MsFormatter {
	
	/**
	 * Create a new formatter object.
	 * @return formatter
	 */
	public static Format createFormatter() {
		return new MsFormat();
	}
	
	/**
	 * Convert a value in miliseconds into a string.
	 * 
	 * @param ms
	 * @return ms as a readable string
	 */
	public static String msToDisplayString(long ms) {
		final Format format = createFormatter();
		return format.format(new Long(ms));
	}
	
	/**
	 * Parse the given string into a value in miliseconds.
	 * 
	 * @param msText
	 * @return time in miliseconds
	 * 
	 * @throws ParseException
	 */
	public static long displayStringToMs(String msText)
		throws ParseException {
		final Format format = createFormatter();
		return ((Long)format.parseObject(msText)).longValue();
	}
}

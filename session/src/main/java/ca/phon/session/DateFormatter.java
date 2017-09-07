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
package ca.phon.session;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;

import ca.phon.formatter.*;

@FormatterType(LocalDate.class)
public class DateFormatter implements Formatter<LocalDate> {
	
	public final static String DATETIME_FORMAT = "yyyy-MM-dd";
	
	/**
	 * Create a date formatter for {@link DateTime} objects.
	 * 
	 * @return formatter
	 */
	public static DateTimeFormatter createFormatter() {
		final DateTimeFormatter retVal = 
				DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		return retVal;
	}
	
	/**
	 * Convert {@link DateTime} objects to string
	 * 
	 * @param dateTime
	 * 
	 * @return text
	 */
	public static String dateTimeToString(LocalDate dateTime) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.format(dateTime);
	}

	/**
	 * Convert {@link DateTime} objects to string
	 * 
	 * @param dateTime
	 * 
	 * @return text
	 */
	public static String dateTimeToString(LocalDateTime dateTime) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.format(dateTime);
	}
	
	/**
	 * Convert a string to a {@link DateTime} object
	 * 
	 * @param text
	 * 
	 * @return dateTime
	 * 
	 * @throws IllegalArgumentException
	 */
	public static LocalDate stringToDateTime(String text) {
		final DateTimeFormatter formatter = createFormatter();
		return LocalDate.parse(text, formatter);
	}

	@Override
	public String format(LocalDate obj) {
		return DateFormatter.dateTimeToString(obj);
	}

	@Override
	public LocalDate parse(String text) throws ParseException {
		return DateFormatter.stringToDateTime(text);
	}
}

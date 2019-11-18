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
package ca.phon.session.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

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

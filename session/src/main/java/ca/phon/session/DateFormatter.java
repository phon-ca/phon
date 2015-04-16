/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

@FormatterType(DateTime.class)
public class DateFormatter implements Formatter<DateTime> {
	
	public final static String DATETIME_FORMAT = "YYYY-MM-DD";
	
	/**
	 * Create a date formatter for {@link DateTime} objects.
	 * 
	 * @return formatter
	 */
	public static DateTimeFormatter createFormatter() {
		final DateTimeFormatter retVal =
				new DateTimeFormatterBuilder()
					.appendYear(4, 4)
					.appendLiteral("-")
					.appendMonthOfYear(2)
					.appendLiteral("-")
					.appendDayOfMonth(2)
					.toFormatter();
		return retVal;
	}
	
	/**
	 * Convert {@link DateTime} objects to string
	 * 
	 * @param dateTime
	 * 
	 * @return text
	 */
	public static String dateTimeToString(DateTime dateTime) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.print(dateTime);
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
	public static DateTime stringToDateTime(String text) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.parseDateTime(text);
	}

	@Override
	public String format(DateTime obj) {
		return DateFormatter.dateTimeToString(obj);
	}

	@Override
	public DateTime parse(String text) throws ParseException {
		return DateFormatter.stringToDateTime(text);
	}
}

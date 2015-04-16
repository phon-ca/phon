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

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * Create formatters for ages stored in {@link Period} objects.
 *
 */
@FormatterType(Period.class)
public class AgeFormatter implements Formatter<Period> {
	
	public final static String AGE_FORMAT = "YY;MM.DD";
	
	/** 
	 * Create a new {@link PeriodFormatter}
	 * 
	 * 
	 */
	public static PeriodFormatter createFormatter() {
		final PeriodFormatter retVal = 
				new PeriodFormatterBuilder()
					.printZeroAlways()
					.minimumPrintedDigits(2).appendYears()
					.appendSeparator(";")
					.minimumPrintedDigits(2).appendMonths()
					.appendSeparator(".")
					.minimumPrintedDigits(2).appendDays()
					.toFormatter();
		return retVal;
	}
	
	/**
	 * Format a {@link Period} object as an age string.
	 * 
	 * @param age
	 * @return age as a string
	 */
	public static String ageToString(Period age) {
		final PeriodFormatter formatter = createFormatter();
		return formatter.print(age);
	}
	
	
	/**
	 * Return an age string as a {@link Period} object
	 * 
	 * @param text
	 * @return age
	 * 
	 * @throws IllegalArgumentException if the string is not formatted 
	 *  correctly
	 */
	public static Period stringToAge(String text) {
		final PeriodFormatter formatter = createFormatter();
		return formatter.parsePeriod(text);
	}

	@Override
	public String format(Period obj) {
		return ageToString(obj);
	}

	@Override
	public Period parse(String text) throws ParseException {
		return stringToAge(text);
	}
}

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
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.phonex.PhonexPattern;

/**
 * Create formatters for ages stored in {@link Period} objects.
 *
 */
@FormatterType(Period.class)
public class AgeFormatter implements Formatter<Period> {
	
	public final static String AGE_FORMAT = "YY;MM.DD";
	
	private final static String AGE_FORMATTER = "";
	
	private final static String AGE_REGEX = "([0-9]{1,2});([0-9]{1,2})\\.([0-9]{1,2})";
	
	/**
	 * Format a {@link Period} object as an age string.
	 * 
	 * @param age
	 * @return age as a string
	 */
	public static String ageToString(Period age) {
		return String.format(AGE_FORMATTER, age.getYears(), age.getMonths(), age.getDays());
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
		final Pattern pattern = Pattern.compile(AGE_REGEX);
		final Matcher matcher = pattern.matcher(text);
		
		if(!matcher.matches()) throw new IllegalArgumentException(text);
		
		final Integer years = Integer.parseInt(matcher.group(1));
		final Integer months = Integer.parseInt(matcher.group(2));
		final Integer days = Integer.parseInt(matcher.group(3));
		
		return Period.of(years, months, days);
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

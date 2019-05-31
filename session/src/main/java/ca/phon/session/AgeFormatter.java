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
package ca.phon.session;

import java.text.ParseException;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * Create formatters for ages stored in {@link Period} objects.
 *
 */
@FormatterType(Period.class)
public class AgeFormatter implements Formatter<Period> {
	
	public final static String AGE_FORMAT = "YY;MM.DD";
	
	private final static String AGE_FORMATTER = "%02d;%02d.%02d";
	
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
		
		if(!matcher.matches()) throw new IllegalArgumentException(
				String.format("Invalid age '%s', format is %s", text, AGE_FORMAT));
		
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

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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class PhonDateFormat extends Format {

	public static final int YEAR_LONG = 0x00;
	public static final int YEAR_SHORT = 0x01;
	
	/* The type of date format */
	private int format;
	
	public PhonDateFormat(int format) {
		super();
		this.format = format;
	}
	
	/**
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String arg0, ParsePosition arg1) {
		Calendar obj = Calendar.getInstance();
		
		// try to parse the object based on the 
		// current format, use a regualar expression
		Pattern datePattern = null;
		if(this.format == YEAR_LONG)
			datePattern = Pattern.compile("([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})");
		else if(this.format == YEAR_SHORT)
			datePattern = Pattern.compile("([0-9]{2})-([0-9]{1,2})-([0-9]{1,2})");
		else
			datePattern = Pattern.compile("");
		
		Matcher matcher = datePattern.matcher(arg0);
		
		if(!matcher.matches())
			throw new PatternSyntaxException("Invalid format", arg0, arg1.getErrorIndex());
		
		String yearString = matcher.group(1);
		String monthString = matcher.group(2);
		String dayString = matcher.group(3);
		
		int year = Integer.parseInt(yearString);
		int month = Integer.parseInt(monthString) - 1;
		int day = Integer.parseInt(dayString);
		
		obj.set(year, month, day);
		
		arg1.setIndex(arg0.length());
		
		return obj;
	}

	/**
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object arg0, StringBuffer arg1,
			FieldPosition arg2) {
		if(!(arg0 instanceof Calendar))
			throw new PatternSyntaxException("Not a java.util.Calendar object", "", 0);
		
		Calendar cal = (Calendar)arg0;
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		
		String yearString = Integer.toString(year);
		String monthString = Integer.toString(month);
		String dayString = Integer.toString(day);
		
		if(this.format == YEAR_SHORT && yearString.length() == 4)
			yearString = yearString.substring(2,4);
		
		if(monthString.length() == 1)
			monthString = "0" + monthString;
		
		if(dayString.length() == 1)
			dayString = "0" + dayString;
		
		arg1.append(yearString + "-" + monthString + "-" + dayString);
			
		return arg1;
	}


}

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <P>Class to format a duration of time.</P>
 * 
 * <P>Possible duration formats are:<BR/>
 * <UL>
 * 	<LI>PHON_FORMAT : Y;M.D</LI>
 *  <LI>XML_FORMAT : P0Y0M0DT0H1M30S</LI>
 * </UL>
 * 
 * <P>This class parses and formats java.util.Calendar objects</P>
 */
public class PhonDurationFormat extends Format {

	public static final int PHON_FORMAT = 0x00;
	public static final int XML_FORMAT = 0x02;
	
	/* The type of date format */
	private int format;
	
	public PhonDurationFormat(int format) {
		super();
		this.format = format;
	}
	
	/**
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String arg0, ParsePosition arg1) {
		PhonDuration obj = new PhonDuration();
		
		if(this.format == PHON_FORMAT)
			obj = parsePhonDuration(arg0, arg1);
		else if(this.format == XML_FORMAT)
			obj = parseXMLDuration(arg0, arg1);
	
		arg1.setIndex(arg0.length());
		
		return obj;
	}
	
	private PhonDuration parsePhonDuration(String data, ParsePosition arg1) {
		//		 try to parse the object based on the 
		// current format, use a regualar expression
		Pattern datePattern = null;
		datePattern = Pattern.compile("([0-9]+);([0-9]+).([0-9]+)");
		
		Matcher matcher = datePattern.matcher(data);
		
		if(!matcher.matches())
			throw new PatternSyntaxException("Invalid format", data, arg1.getErrorIndex());
		
		String yearString = matcher.group(1);
		String monthString = matcher.group(2);
		String dayString = matcher.group(3);
		
		int year = Integer.parseInt(yearString);
		int month = Integer.parseInt(monthString);
		int day = Integer.parseInt(dayString);
		
		PhonDuration duration = new PhonDuration(year, month, day);
		return duration;
	}
	
	private PhonDuration parseXMLDuration(String data, ParsePosition arg1) {
		Pattern datePattern = Pattern.compile("(-)?P" + // optional minus sign and required P
				"(?:([0-9]+)Y)?" + // year
				"(?:([0-9]+)M)?" + // month
				"(?:([0-9]+)D)?" + // day
				"(?:T"  + // if we have time variables
				"(?:([0-9]+)H)?" + // hours
				"(?:([0-9]+)M)?" + // minutes
				"(?:([0-9]+)S)?" + // seconds
				")?");
				
		Matcher matcher = datePattern.matcher(data);
	
		if(!matcher.matches())
			throw new PatternSyntaxException("Invalid format", data, arg1.getErrorIndex());
		
		PhonDuration duration = new PhonDuration();
		/*
		 * Groups are defined as follows:
		 *   0 - The entire data string
		 *   1 - Negative
		 *   2 - Number of years
		 *   3 - Number of months
		 *   4 - Number of days
		 *   5 - Number of hours
		 *   6 - Number of minutes
		 *   7 - Number of seconds
		 */
		for(int i = 0; i <= matcher.groupCount(); i++) {
			if(matcher.group(i) != null) {
				switch(i) {
				case 1:
					duration.setNegative(true);
					break;
				case 2:
					duration.setYears(Integer.parseInt(matcher.group(i)));
					break;
				case 3:
					duration.setMonths(Integer.parseInt(matcher.group(i)));
					break;
				case 4:
					duration.setDays(Integer.parseInt(matcher.group(i)));
					break;
				case 5:
					duration.setHours(Integer.parseInt(matcher.group(i)));
					break;
				case 6:
					duration.setMinutes(Integer.parseInt(matcher.group(i)));
					break;
				case 7:
					duration.setSeconds(Integer.parseInt(matcher.group(i)));
					break;
				default:
					break;
				}
			}
		}
		
		
		String negative = matcher.group(1);
		String years = matcher.group(2);
		String months = matcher.group(3);
		String days = matcher.group(4);
		String hours = matcher.group(5);
		String minutes = matcher.group(6);
		String seconds = matcher.group(7);
		
		return duration;
	}

	/**
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object arg0, StringBuffer arg1,
			FieldPosition arg2) {
		if(!(arg0 instanceof PhonDuration))
			throw new PatternSyntaxException("Not a util.Duration object", "", 0);
		
		PhonDuration duration = (PhonDuration)arg0;
		
		
		String yearString = String.format("%02d", duration.getYears());
			// Integer.toString(duration.getYears());
		String monthString = String.format("%02d", duration.getMonths());
			// Integer.toString(duration.getMonths());
		String dayString = String.format("%02d", duration.getDays());
			// Integer.toString(duration.getDays());
		String hourString = Integer.toString(duration.getHours());
		String minuteString = Integer.toString(duration.getMinutes());
		String secondString = Integer.toString(duration.getSeconds());
		
		if(this.format == PHON_FORMAT) 
			arg1.append(yearString + ";" + monthString + "." + dayString);
		else if(this.format == XML_FORMAT)
			arg1.append((duration.isNegative() ? "-" : "") + "P" + 
					yearString + "Y" +
					monthString + "M" + 
					dayString  + "D" +
					"T" + 
					hourString + "H" +
					minuteString + "M" +
					secondString + "S");
			
		return arg1;
	}
}

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
package ca.phon.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Text formatter for milliseconds.
 *
 */
public class MsFormat extends Format {

	private static final long serialVersionUID = 4432179935812692306L;
	
	private final static String PATTERN = "([0-9]{3})\\:([0-9]{2})\\.([0-9]{3})";

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		Long toParse = null;
		
		if(obj instanceof Integer) toParse = ((Integer)obj).longValue();
		if(obj instanceof Long) toParse = (Long)obj;
		
		StringBuffer retVal = new StringBuffer();
		retVal.append(this.msToDisplayString(toParse));
		
		return retVal;
	}

	protected String msToDisplayString(long ms) 
		throws IllegalArgumentException {
		if(ms < 0)
			throw new IllegalArgumentException("Time cannot be negative.");
		
		long numSeconds = ms / 1000;
		long numMSecondsLeft = ms % 1000;
		
		long numMinutes = numSeconds / 60;
		long numSecondsLeft = numSeconds % 60;
		
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(2);
		
		NumberFormat msNf = NumberFormat.getIntegerInstance();
		msNf.setMinimumIntegerDigits(3);
		
		String minuteString = msNf.format(numMinutes) + ":";
		
		String secondString =
			(numMinutes == 0 
					? (nf.format(numSeconds) + ".")
					: (nf.format(numSecondsLeft) + ".")
			);
		
		String msString = 
			(msNf.format(numMSecondsLeft));
		
		String timeString = 
			minuteString + secondString + msString;
		
		return timeString;
	}
	
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		Object retVal = null;
		final Pattern pattern = Pattern.compile(PATTERN);
		final Matcher matcher = pattern.matcher(source);
		if(matcher.matches()) {
			final String minString = matcher.group(1);
			final int mins = Integer.parseInt(minString);
			
			final String secString = matcher.group(2);
			final int secs = Integer.parseInt(secString);
			
			final String msString = matcher.group(3);
			final int ms = Integer.parseInt(msString);
			
			retVal = new Long(
					ms + (secs * 1000) + (mins * 60 * 1000));
			pos.setIndex(source.length());
		}
		return retVal;
	}

}

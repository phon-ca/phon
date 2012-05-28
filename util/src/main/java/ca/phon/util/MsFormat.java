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
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.logging.Logger;


/**
 * Text formatter for milliseconds.
 *
 */
public class MsFormat extends Format {

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		if(!(obj instanceof Integer))
			throw new IllegalArgumentException("Can only format Integers");
		
		StringBuffer retVal = new StringBuffer();
		retVal.append(this.msToDisplayString((Integer)obj));
		
		return retVal;
	}

	protected String msToDisplayString(int ms) 
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
		
		String minuteString = 
			(numMinutes < 100 ? nf.format(numMinutes) : msNf.format(numMinutes)) + ":";
		
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
		Logger.getLogger(getClass().getName()).warning("Method parseObject(String, ParsePosition) not implemented.");
		return new Integer(0);
	}

}

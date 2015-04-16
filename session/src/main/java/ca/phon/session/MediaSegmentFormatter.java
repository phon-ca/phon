/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.util.MsFormatter;

@FormatterType(MediaSegment.class)
public class MediaSegmentFormatter implements Formatter<MediaSegment> {

	@Override
	public String format(MediaSegment obj) {
		final long startTime = ((Float)obj.getStartValue()).longValue();
		final long endTime = ((Float)obj.getEndValue()).longValue();
		
		final String startTimeText = MsFormatter.msToDisplayString(startTime);
		final String endTimeText = MsFormatter.msToDisplayString(endTime);
		
		return String.format("%s-%s", startTimeText, endTimeText);
	}

	@Override
	public MediaSegment parse(String text) throws ParseException {
		final String[] parts = text.split("-");
		if(parts.length != 2) throw new ParseException(text, 0);
		final long startTime = MsFormatter.displayStringToMs(parts[0]);
		final long endTime = MsFormatter.displayStringToMs(parts[1]);
		
		final SessionFactory factory = SessionFactory.newFactory();
		final MediaSegment retVal = factory.createMediaSegment();
		retVal.setStartValue(startTime);
		retVal.setEndValue(endTime);
		retVal.setUnitType(MediaUnit.Millisecond);
		return retVal;
	}

}

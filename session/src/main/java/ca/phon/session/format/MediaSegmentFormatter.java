/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.session.format;

import ca.phon.formatter.*;
import ca.phon.session.*;

import java.text.ParseException;

@FormatterType(MediaSegment.class)
public class MediaSegmentFormatter implements Formatter<MediaSegment> {

	private final MediaTimeFormatStyle formatStyle;

	public MediaSegmentFormatter() {
		this(MediaTimeFormatStyle.PADDED_MINUTES_AND_SECONDS);
	}

	public MediaSegmentFormatter(MediaTimeFormatStyle formatStyle) {
		super();
		this.formatStyle = formatStyle;
	}

	@Override
	public String format(MediaSegment obj) {
		final long startTime = ((Float)obj.getStartValue()).longValue();
		final long endTime = ((Float)obj.getEndValue()).longValue();

		final MediaTimeFormat format = new MediaTimeFormat(formatStyle);
		final String startTimeText = format.format(startTime);
		final String endTimeText = format.format(endTime);
		
		return String.format("%s-%s", startTimeText, endTimeText);
	}

	@Override
	public MediaSegment parse(String text) throws ParseException {
		final String[] parts = text.split("-");
		if(parts.length != 2) throw new ParseException(text, 0);

		final MediaTimeFormat format = new MediaTimeFormat(formatStyle);
		final long startTime = (long)format.parseObject(parts[0]);
		final long endTime = (long)format.parseObject(parts[1]);
		
		final SessionFactory factory = SessionFactory.newFactory();
		final MediaSegment retVal = factory.createMediaSegment();
		retVal.setStartValue(startTime);
		retVal.setEndValue(endTime);
		retVal.setUnitType(MediaUnit.Millisecond);
		return retVal;
	}

}

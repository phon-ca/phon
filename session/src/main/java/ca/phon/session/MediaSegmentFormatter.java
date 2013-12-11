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

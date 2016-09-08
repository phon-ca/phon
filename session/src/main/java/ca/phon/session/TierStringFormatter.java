package ca.phon.session;

import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

@FormatterType(TierString.class)
public class TierStringFormatter implements Formatter<TierString> {

	@Override
	public String format(TierString obj) {
		return obj.toString();
	}

	@Override
	public TierString parse(String text) throws ParseException {
		return new TierString(text);
	}

}

package ca.phon.ipa;

import java.text.ParseException;

import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;

/**
 * IPA transcript formatter
 */
@FormatterType(IPATranscript.class)
public class IPATranscriptFormatter implements Formatter<IPATranscript> {

	@Override
	public String format(IPATranscript obj) {
		return obj.toString();
	}

	@Override
	public IPATranscript parse(String text) throws ParseException {
		return IPATranscript.parseTranscript(text);
	}
	
}

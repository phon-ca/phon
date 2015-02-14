package ca.phon.formatter;

import java.text.ParseException;

/**
 * Basic formatter which simply returns the given string
 * value.
 * 
 * 
 */
@FormatterType(String.class)
public class StringFormatter implements Formatter<Object> {

	@Override
	public String format(Object obj) {
		return obj.toString();
	}

	@Override
	public Object parse(String text) throws ParseException {
		return text;
	}
	
}

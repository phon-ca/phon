package ca.phon.media;

import java.text.Format;
import java.text.ParseException;

import ca.phon.util.MsFormat;


/**
 * Format time values in miliseconds into parseable strings.
 */
public class MsFormatter {
	
	/**
	 * Create a new formatter object.
	 * @return formatter
	 */
	public static Format createFormatter() {
		return new MsFormat();
	}
	
	/**
	 * Convert a value in miliseconds into a string.
	 * 
	 * @param ms
	 * @return ms as a readable string
	 */
	public static String msToDisplayString(long ms) {
		final Format format = createFormatter();
		return format.format(new Long(ms));
	}
	
	/**
	 * Parse the given string into a value in miliseconds.
	 * 
	 * @param msText
	 * @return time in miliseconds
	 * 
	 * @throws ParseException
	 */
	public static long displayStringToMs(String msText)
		throws ParseException {
		final Format format = createFormatter();
		return ((Long)format.parseObject(msText)).longValue();
	}
}

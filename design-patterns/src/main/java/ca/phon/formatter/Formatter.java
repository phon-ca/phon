package ca.phon.formatter;

import java.text.ParseException;

/**
 * Base interface for implementing formatters.  {@link Formatter}s are classes
 * that convert to/from a paticular type of object/string.
 * 
 * Implementing classes should register their {@link Formatter} by adding
 * the full class name to the file: META-INF/services/ca.phon.formatter.Formatter 
 */
public interface Formatter<T> {
	
	/**
	 * Convert the given object into a formatted String.
	 * 
	 * @param obj
	 * @return formatted string
	 */
	public String format(T obj);
	
	/**
	 * Parse the given string into a new object instance
	 * 
	 * @param text
	 * @return parsed object
	 * 
	 * @throws ParseException if there was a problem
	 *  parsing the given text
	 */
	public T parse(String text) throws ParseException;
	
}

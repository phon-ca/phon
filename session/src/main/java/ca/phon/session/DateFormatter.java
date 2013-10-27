package ca.phon.session;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class DateFormatter {
	
	/**
	 * Create a date formatter for {@link DateTime} objects.
	 * 
	 * @return formatter
	 */
	public static DateTimeFormatter createFormatter() {
		final DateTimeFormatter retVal =
				new DateTimeFormatterBuilder()
					.appendYear(4, 4)
					.appendLiteral("-")
					.appendMonthOfYear(2)
					.appendLiteral("-")
					.appendDayOfMonth(2)
					.toFormatter();
		return retVal;
	}
	
	/**
	 * Convert {@link DateTime} objects to string
	 * 
	 * @param dateTime
	 * 
	 * @return text
	 */
	public static String dateTimeToString(DateTime dateTime) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.print(dateTime);
	}

	/**
	 * Convert a string to a {@link DateTime} object
	 * 
	 * @param text
	 * 
	 * @return dateTime
	 * 
	 * @throws IllegalArgumentException
	 */
	public static DateTime stringToDateTime(String text) {
		final DateTimeFormatter formatter = createFormatter();
		return formatter.parseDateTime(text);
	}
}

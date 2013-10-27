package ca.phon.session;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;
import org.joda.time.format.PeriodPrinter;

/**
 * Create formatters for ages stored in {@link Period} objects.
 *
 */
public class AgeFormatter {
	
	/** 
	 * Create a new {@link PeriodFormatter}
	 * 
	 * 
	 */
	public static PeriodFormatter createFormatter() {
		final PeriodFormatter retVal = 
				new PeriodFormatterBuilder()
					.printZeroAlways()
					.minimumPrintedDigits(2).appendYears()
					.appendLiteral(";")
					.minimumPrintedDigits(2).appendMonths()
					.appendLiteral(".")
					.minimumPrintedDigits(2).appendDays()
					.toFormatter();
		return retVal;
	}
	
	/**
	 * Format a {@link Period} object as an age string.
	 * 
	 * @param age
	 * @return age as a string
	 */
	public static String ageToString(Period age) {
		final PeriodFormatter formatter = createFormatter();
		return formatter.print(age);
	}
	
	
	/**
	 * Return an age string as a {@link Period} object
	 * 
	 * @param text
	 * @return age
	 * 
	 * @throws IllegalArgumentException if the string is not formatted 
	 *  correctly
	 */
	public static Period stringToAge(String text) {
		final PeriodFormatter formatter = createFormatter();
		return formatter.parsePeriod(text);
	}
}

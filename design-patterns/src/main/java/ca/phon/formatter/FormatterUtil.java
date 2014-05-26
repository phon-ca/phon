package ca.phon.formatter;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormatterUtil {
	
	private final static Logger LOGGER = Logger
			.getLogger(FormatterUtil.class.getName());

	public static Object parse(Class<?> typ, String txt) {
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(typ);
		if(formatter != null) {
			try {
				return formatter.parse(txt);
			} catch (ParseException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String format(Object obj) {
		final Class<?> typ = obj.getClass();
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(typ);
		if(formatter != null) {
			return formatter.format(obj);
		} else {
			return obj.toString();
		}
	}
	
}

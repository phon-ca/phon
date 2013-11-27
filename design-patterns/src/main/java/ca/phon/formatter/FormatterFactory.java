package ca.phon.formatter;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Provides classpath access to formatters based on entries
 * found in the META-INF/services/ca.phon.formatter.Formatter
 * files.
 */
public class FormatterFactory {
	
	/**
	 * Create a new formatter for the given type.
	 * 
	 * @param type
	 * @return formatter for the given type of <code>null</code>
	 *  if a formatter was not found
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Formatter<T> createFormatter(Class<T> type) {
		Formatter<T> retVal = null;
		final ServiceLoader<Formatter> formatterLoader = ServiceLoader.load(Formatter.class);
		final Iterator<Formatter> formatterItr = formatterLoader.iterator();
		while(formatterItr.hasNext()) {
			final Formatter<?> formatter = formatterItr.next();
			final FormatterType formatterType = formatter.getClass().getAnnotation(FormatterType.class);
			if(formatterType != null) {
				if(formatterType.value() == type) {
					retVal = (Formatter<T>) formatter;
					break;
				}
			}
		}
		return retVal;
	}
	
}

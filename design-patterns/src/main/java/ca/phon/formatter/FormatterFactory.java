/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.formatter;

import java.util.*;

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
				if(formatterType.value().isAssignableFrom(type)) {
					retVal = (Formatter<T>) formatter;
					break;
				}
			}
		}
		return retVal;
	}
	
}

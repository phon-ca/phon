/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.formatter;

import java.util.*;

/**
 * Provides classpath access to formatters based on entries
 * found in the META-INF/services/ca.phon.formatter.Formatter
 * files.
 */
public class FormatterFactory {

	private static final List<Formatter<?>> formatters = new ArrayList<>();

	static {
		final ServiceLoader<Formatter> formatterLoader = ServiceLoader.load(Formatter.class);
		final Iterator<Formatter> formatterItr = formatterLoader.iterator();
		while(formatterItr.hasNext()) {
			final Formatter<?> formatter = formatterItr.next();
			formatters.add(formatter);
		}
	}
	
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

		Optional<Formatter<?>> formatter =
				formatters.parallelStream()
					.filter( f ->  {
						FormatterType ft = f.getClass().getAnnotation(FormatterType.class);
						if(ft != null) {
							return ft.value().isAssignableFrom(type);
						} else {
							return false;
						}
					}).findFirst();

		return (formatter.isPresent() ? (Formatter<T>) formatter.get() : null);
	}
	
}

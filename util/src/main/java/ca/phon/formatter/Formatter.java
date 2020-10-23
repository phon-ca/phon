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

import java.text.*;

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

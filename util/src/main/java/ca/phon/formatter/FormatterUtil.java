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

import org.apache.logging.log4j.LogManager;

import java.text.ParseException;

public class FormatterUtil {
	
	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(FormatterUtil.class.getName());

	public static Object parse(Class<?> typ, String txt) {
		@SuppressWarnings("unchecked")
		final Formatter<Object> formatter = 
				(Formatter<Object>)FormatterFactory.createFormatter(typ);
		if(formatter != null) {
			try {
				return formatter.parse(txt);
			} catch (ParseException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static String format(Object obj) {
		if(obj == null) return "";
		
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

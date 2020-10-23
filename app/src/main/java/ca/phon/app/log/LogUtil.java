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
package ca.phon.app.log;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.LogManager;

/**
 * Utility functions for logging.
 * 
 */
public final class LogUtil {
	
	/* Info/trace */
	public static void info(String message) {
		log(Level.INFO, message);
	}
	
	public static void info(Throwable e) {
		severe(e.getLocalizedMessage(), e);
	}
	
	public static void info(Class<?> clazz, Throwable e) {
		severe(clazz, e.getLocalizedMessage(), e);
	}
	
	public static void info(String message, Throwable e) {
		severe(null, message, e);
	}
	
	public static void info(Class<?> clazz, String message, Throwable e) {
		log(clazz, Level.TRACE, message, e);
	}

	/* Warning */
	public static void warning(String message) {
		log(Level.WARN, message);
	}
	
	public static void warning(Throwable e) {
		severe(e.getLocalizedMessage(), e);
	}
	
	public static void warning(Class<?> clazz, Throwable e) {
		severe(clazz, e.getLocalizedMessage(), e);
	}
	
	public static void warning(String message, Throwable e) {
		severe(null, message, e);
	}
	
	public static void warning(Class<?> clazz, String message, Throwable e) {
		log(clazz, Level.WARN, message, e);
	}
	
	/* Severe */
	public static void severe(String message) {
		log(Level.ERROR, message);
	}
	
	public static void severe(Throwable e) {
		severe(e.getLocalizedMessage(), e);
	}
	
	public static void severe(Class<?> clazz, Throwable e) {
		severe(clazz, e.getLocalizedMessage(), e);
	}
	
	public static void severe(String message, Throwable e) {
		severe(null, message, e);
	}
	
	public static void severe(Class<?> clazz, String message, Throwable e) {
		log(clazz, Level.ERROR, message, e);
	}
	
	/* General */
	public static void log(Level level, String message, Throwable e) {
		log(null, level, message, e);
	}
	
	public static void log(Class<?> clazz, Level level, String message, Throwable e) {
		org.apache.logging.log4j.Logger logger = LogManager.getLogger((clazz == null ? "ca.phon" : clazz.getName()));
		logger.log(level, message, e);
	}
	
	public static void log(Level level, String message) {
		log(null, level, message);
	}
	
	public static void log(Class<?> clazz, Level level, String message) {
		org.apache.logging.log4j.Logger logger = LogManager.getLogger((clazz == null ? "ca.phon" : clazz.getName()));
		logger.log(level, message);
	}
}
	

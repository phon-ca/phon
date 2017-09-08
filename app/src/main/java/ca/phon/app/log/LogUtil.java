package ca.phon.app.log;

import java.util.logging.Level;

/**
 * Utility functions for logging.
 * 
 */
public final class LogUtil {
	
	/* Info */
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
		log(clazz, Level.INFO, message, e);
	}

	/* Warning */
	public static void warning(String message) {
		log(Level.WARNING, message);
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
		log(clazz, Level.WARNING, message, e);
	}
	
	/* Severe */
	public static void severe(String message) {
		log(Level.SEVERE, message);
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
		log(clazz, Level.SEVERE, message, e);
	}
	
	/* General */
	public static void log(Level level, String message, Throwable e) {
		log(null, level, message, e);
	}
	
	public static void log(Class<?> clazz, Level level, String message, Throwable e) {
		final java.util.logging.Logger logger =
				(clazz == null ? java.util.logging.Logger.getLogger("ca.phon") : java.util.logging.Logger.getLogger(clazz.getName()));
		logger.log(level, message, e);
	}
	
	public static void log(Level level, String message) {
		log(null, level, message);
	}
	
	public static void log(Class<?> clazz, Level level, String message) {
		final java.util.logging.Logger logger =
				(clazz == null ? java.util.logging.Logger.getLogger("ca.phon") : java.util.logging.Logger.getLogger(clazz.getName()));
		logger.log(level, message);
	}
}
	

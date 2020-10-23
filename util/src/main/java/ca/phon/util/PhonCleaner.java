package ca.phon.util;

import java.lang.ref.*;

/**
 * Wrapper method for a static cleaner for the application.
 */
public class PhonCleaner {

	private final static Cleaner cleaner = Cleaner.create();
	
	public static void register(Object obj, Runnable action) {
		cleaner.register(obj, action);
	}
	
}

package ca.phon.util;

import java.text.Collator;
import java.util.Locale;

/**
 * Provides static methods for getting
 * collators based on locale settings.
 *
 */
public class CollatorFactory {
	
	/**
	 * Create a new collator for the default
	 * locale.
	 */
	public static Collator defaultCollator() {
		return collator(Locale.getDefault());
	}
	
	/**
	 * Create a new collator with the given locale.
	 * 
	 */
	public static Collator collator(Locale locale) {
		return collator(locale, Collator.TERTIARY);
	}
	
	/**
	 * Create a new collator with the given locale
	 * and strength.
	 */
	public static Collator collator(Locale locale, int strength) {
		Collator retVal = Collator.getInstance(locale);
		retVal.setStrength(strength);
		return retVal;
	}

}

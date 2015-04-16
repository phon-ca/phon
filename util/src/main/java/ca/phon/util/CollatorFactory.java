/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

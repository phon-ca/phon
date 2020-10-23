/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.util;

import java.text.*;
import java.util.*;

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

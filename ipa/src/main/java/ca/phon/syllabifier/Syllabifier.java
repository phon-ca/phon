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
package ca.phon.syllabifier;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.syllable.*;
import ca.phon.util.*;

/**
 * Provides methods for applying the {@link SyllableConstituentType}
 * annotations on {@link IPAElement}s.
 * 
 */
public interface Syllabifier {
	
	/**
	 * Syllabifier name.  Preferably unique
	 * for identify syllabifiers in the UI.
	 * 
	 * @return name
	 */
	public String getName();
	
	/**
	 * Syllabifier language.
	 * 
	 * @return language for the syllabifier
	 */
	public Language getLanguage();
	
	/**
	 * Apply consituent type annotations
	 * on given phones.
	 * 
	 * @param phones
	 */
	public void syllabify(List<IPAElement> phones);
	
}

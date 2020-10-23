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
package ca.phon.ipadictionary.spi;

import ca.phon.extensions.*;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.exceptions.*;

/**
 * IPADictionary capability for adding a new
 * orthography->ipa entry.
 *
 */
@Extension(IPADictionary.class)
public interface AddEntry {
	
	/**
	 * Add a new entry to the ipa dictionary
	 * 
	 * @param orthography
	 * @param ipa
	 * @throws IPADictionaryExecption if the entry was
	 *  not added to the dictionary.  E.g., the key->value
	 *  pair already exists or the dictionary was not able
	 *  to add the entry to it's storage.
	 */
	public void addEntry(String orthography, String ipa)
		throws IPADictionaryExecption;
	
}

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
 * Remove an orthography->ipa pair from the
 * dictionary.  Not all dictionaries support
 * removing entries.
 * 
 */
@Extension(IPADictionary.class)
public interface RemoveEntry {

	/**
	 * Remove the specified entry from the 
	 * dictionary.  Does nothing if the
	 * entry was not found.
	 * 
	 * @param orthography
	 * @param ipa
	 * @throws IPADictionaryExecption if the key->value was not
	 *  removed from the database.  This will usually occur if
	 *  there was a problem with dictionary storage.
	 */
	public void removeEntry(String orthography, String ipa)
		throws IPADictionaryExecption;
	
}

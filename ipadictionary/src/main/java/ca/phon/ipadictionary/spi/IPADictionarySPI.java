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

import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.exceptions.*;

/**
 * Required interface for IPADictionary implementations.
 * IPADictionaries are expected to accept orthographic strings
 * and return one or more associated IPA transcriptions.
 * 
 */
public interface IPADictionarySPI {
	
	/**
	 * Lookup IPA transcriptions for a given
	 * orthographic string.
	 * 
	 * @param orthography
	 * @return a list of IPA transcriptions associated
	 *  with the given orthography
	 * @throws IPADictionary exception if an error occured
	 *  while attempting to lookup the given entry
	 */
	public String[] lookup(String orthography)
		throws IPADictionaryExecption;

	/**
	 * Install this SPI into the given IPADictionary
	 */
	public void install(IPADictionary dict);
}

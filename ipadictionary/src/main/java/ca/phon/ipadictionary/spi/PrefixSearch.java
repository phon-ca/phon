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

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Performs a search of keys (i.e., orthography) of the dictionary
 * and returns all keys which have the given prefix.
 * 
 */
@Extension(IPADictionary.class)
public interface PrefixSearch {
	
	/**
	 * Search for all instances of the given
	 * prefix in orthographic keys.
	 * 
	 * @param prefix
	 * @return a list of orthographic keys which
	 *  have the specified prefix
	 */
	public String[] keysWithPrefix(String prefix);

}

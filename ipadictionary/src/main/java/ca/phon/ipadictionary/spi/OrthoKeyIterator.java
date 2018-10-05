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

import java.util.Iterator;

import ca.phon.extensions.Extension;
import ca.phon.ipadictionary.IPADictionary;

/**
 * Capability for iterating the orthographic keys found
 * in the dictionary.
 * 
 */
@Extension(IPADictionary.class)
public interface OrthoKeyIterator {
	
	/**
	 * Return an iterator for the keys found
	 * in this dictionary.  Order of keys returned
	 * by the iterator is determined by dictionary
	 * implementation and is not guaranteed.
	 * 
	 * @return the key iterator
	 */
	public Iterator<String> iterator();

}

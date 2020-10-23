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

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.ipadictionary.*;

/**
 * Metadata consists of a map of string to string
 * values.
 */
@Extension(IPADictionary.class)
public interface Metadata {
	
	/**
	 * Get value for a given metadata key.
	 * 
	 * @param key the metadata key.  Common keys are
	 *  'provider' and 'website'
	 * @return the value for the specified key or <code>null</code>
	 *  if no data is available. See {@link #metadataKeyIterator()}
	 */
	public String getMetadataValue(String key);
	
	/**
	 * Get the iteator for metadata keys.
	 * 
	 * @return an iterator for the metadata keys available
	 */
	public Iterator<String> metadataKeyIterator();

}

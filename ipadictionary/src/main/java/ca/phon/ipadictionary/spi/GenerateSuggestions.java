/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

/**
 * Dictionary capability for generating ipa
 * suggestions. Suggestions are not validated
 * and may be very inaccurate - use at own risk
 *
 */
@Extension(IPADictionary.class)
public interface GenerateSuggestions {

	/**
	 * Generate a list of suggestions for a given
	 * orthography.  If the given orthography appears in the 
	 * dictionary as-is this method returns the same
	 * as lookup.
	 * 
	 * @param orthography
	 * @return a list of generated ipa suggestions
	 * 
	 */
	public String[] generateSuggestions(String orthography);
	
}

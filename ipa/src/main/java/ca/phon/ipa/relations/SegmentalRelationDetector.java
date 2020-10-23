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
package ca.phon.ipa.relations;

import java.util.*;

import ca.phon.ipa.alignment.*;

public interface SegmentalRelationDetector {
	
	/**
	 * Returns an optional segmental relation.  isPresent()
	 * will return <code>false</code> when relation was
	 * not detected.
	 * 
	 * 
	 * @param pm
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Optional<SegmentalRelation> detect(PhoneMap pm, int p1, int p2);
	
}

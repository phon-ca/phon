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
package ca.phon.session.filter;

import java.util.*;

import ca.phon.session.Record;

/**
 * Abstract implementation of utterance filter.
 * Includes helper method for filtering a list
 * of utterances.
 *
 */
public abstract class AbstractRecordFilter implements RecordFilter {

	/**
	 * Filter the given list of utterances.  The given list
	 * is not changed.
	 * 
	 * @param utts
	 * @return a new list containing the filtered utterances
	 */
	public List<Record> filterUtterances(List<Record> utts) {
		List<Record> retVal = new ArrayList<Record>();
		
		for(Record utt:utts) {
			if(checkRecord(utt)) {
				retVal.add(utt);
			}
		}
		
		return retVal;
	}
	
}

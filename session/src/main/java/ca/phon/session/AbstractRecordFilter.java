/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.session;

import java.util.ArrayList;
import java.util.List;

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
			if(checkUtterance(utt)) {
				retVal.add(utt);
			}
		}
		
		return retVal;
	}
	
}

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

package ca.phon.query.db;

import java.util.HashSet;
import java.util.Set;

import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.filter.AbstractRecordFilter;


/**
 * Filter a list of records using a search.
 */
public class ResultSetRecordFilter extends AbstractRecordFilter {
	/** The search */
	private ResultSet resultSet;
	
	/** Transcript */
	private Session t;
	
	Set<Integer> resultRecords = new HashSet<Integer>();
	
	public ResultSetRecordFilter(Session t, ResultSet s) {
		this.resultSet = s;
		this.t = t;
		for(Result r : resultSet)
			resultRecords.add(r.getRecordIndex());
	}

	@Override
	public boolean checkRecord(Record utt) {
		int uttIdx = t.getRecordPosition(utt);
		return (uttIdx >= 0 && resultRecords.contains(uttIdx));
	}
}

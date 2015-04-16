/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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

package ca.phon.query.db;

import java.util.HashSet;
import java.util.Set;

import ca.phon.session.AbstractRecordFilter;
import ca.phon.session.Record;
import ca.phon.session.Session;


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

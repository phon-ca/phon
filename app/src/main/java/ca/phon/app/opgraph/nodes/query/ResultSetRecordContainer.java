/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.query;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ca.phon.app.opgraph.nodes.RecordContainer;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultSet;
import ca.phon.session.Session;

public class ResultSetRecordContainer implements RecordContainer {

	private Session session;
	
	private ResultSet resultSet;

	public ResultSetRecordContainer(Session session, ResultSet resultSet) {
		super();
		
		this.session = session;
		this.resultSet = resultSet;
	}

	@Override
	public Session getSession() {
		return this.session;
	}

	@Override
	public Iterator<Integer> idxIterator() {
		Set<Integer> idxSet = new LinkedHashSet<>();
		for(Result result:this.resultSet) {
			idxSet.add(result.getRecordIndex());
		}
		return idxSet.iterator();
	}

}

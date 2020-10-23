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
package ca.phon.app.opgraph.nodes.query;

import java.util.*;

import ca.phon.app.opgraph.nodes.*;
import ca.phon.query.db.*;
import ca.phon.session.*;

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

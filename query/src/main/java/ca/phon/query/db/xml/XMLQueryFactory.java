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

package ca.phon.query.db.xml;

import ca.phon.project.Project;
import ca.phon.query.db.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * XML-based implementation of {@link QueryFactory}.
 */
public class XMLQueryFactory implements QueryFactory {
	
	@Override
	public Query createQuery() {
		// Ensure certain attributes exist with default values 
		XMLQuery query = new XMLQuery();
		query.setUUID(UUID.randomUUID());
		query.setDate(LocalDateTime.now());
		return query;
	}
	
	@Override
	public Query createQuery(Project project) {
		// Get the next available number
		int max = 0;
		File queryPath = XMLResultSetManager.getQueriesPath(project);
		if(!queryPath.exists() && !queryPath.mkdirs())
			return null;
		
		for(String query : queryPath.list()) {
			if(query.startsWith("query")) {
				try {
					max = Math.max(max, Integer.parseInt(query.substring(5)));
				} catch(NumberFormatException exc) { /* don't need to deal with this */ }
			}
		}
		
		++max;
		
		// Ensure certain attributes exist with default values 
		XMLQuery query = new XMLQuery();
		query.setName(String.format("query%03d", max));
		query.setUUID(UUID.randomUUID());
		query.setDate(LocalDateTime.now());
		return query;
	}

	@Override
	public ResultSet createResultSet() {
		return new XMLResultSet();
	}

	@Override
	public Result createResult() {
		return new XMLResult();
	}

	@Override
	public Script createScript() {
		return new XMLScript();
	}

	@Override
	public ResultValue createResultValue() {
		return new XMLResultValue();
	}

}

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

package ca.phon.query.db.xml;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import ca.phon.project.Project;
import ca.phon.query.db.*;

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

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
package ca.phon.query.analysis;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ca.phon.query.db.ResultSet;
import ca.phon.session.SessionPath;

public class QueryAnalysisResult {
	
	private final QueryAnalysisInput input;
	
	private final Map<SessionPath, ResultSet> queryResults = 
			Collections.synchronizedMap(new LinkedHashMap<SessionPath, ResultSet>());
	
	private final Map<SessionPath, String> queryOutput = 
			Collections.synchronizedMap(new LinkedHashMap<SessionPath, String>());

	public QueryAnalysisResult(QueryAnalysisInput input) {
		this.input = input;
	}
	
	public QueryAnalysisInput getInput() {
		return this.input;
	}
			
	public Map<SessionPath, ResultSet> getQueryResults() {
		return queryResults;
	}

	public Map<SessionPath, String> getQueryOutput() {
		return queryOutput;
	}
	
	public void putResultSet(SessionPath location, ResultSet rs) {
		getQueryResults().put(location, rs);
	}
	
	public ResultSet getResultSet(SessionPath location) {
		return getQueryResults().get(location);
	}
	
	public void putOutput(SessionPath location, String output) {
		getQueryOutput().put(location, output);
	}
	
	public String getOutput(SessionPath location) {
		return getQueryOutput().get(location);
	}
	
	public Set<SessionPath> getResultSetKeys() {
		return this.queryResults.keySet();
	}
	
	public Set<SessionPath> getOutputKeys() {
		return this.queryOutput.keySet();
	}
	
}

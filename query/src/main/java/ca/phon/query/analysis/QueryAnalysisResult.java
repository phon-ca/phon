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

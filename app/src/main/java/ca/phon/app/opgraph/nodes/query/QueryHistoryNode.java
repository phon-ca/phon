/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.session.SessionPath;

@OpNodeInfo(
		name="Query History",
		description="Load results from query history",
		category="Query"
)
public class QueryHistoryNode extends OpNode {

	private InputField projectInput = new InputField("project", "Phon project", true, true, Project.class);

	private InputField queryIdInput = new InputField("queryId", "Query history id", true, true, String.class);
	
	private InputField selectedResultsInput = new InputField("selectedResults", "Selected result sets", true, true, Collection.class);

	private OutputField projectOutput = new OutputField("project", "Phon project", true, Project.class);

	private OutputField queryField = new OutputField("query",
			"Query parameters", true, Query.class);

	private OutputField outputField = new OutputField("result sets",
			"Result set, one per input record container", true, ResultSet[].class);

	public QueryHistoryNode() {
		super();

		putField(projectInput);
		putField(queryIdInput);
		putField(selectedResultsInput);

		putField(projectOutput);
		putField(queryField);
		putField(outputField);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void operate(OpContext context) throws ProcessingException {
		Project project = null;
		if(context.get(projectInput) != null) {
			project = (Project)context.get(projectInput);
		} else if(context.containsKey("_project") && context.get("_project") instanceof Project) {
			project = (Project)context.get("_project");
		} else {
			throw new ProcessingException(null, "No project given");
		}

		String queryId = "";
		if(context.get(queryIdInput) != null) {
			queryId = (String)context.get(queryIdInput);
		} else if(context.containsKey("_queryId")) {
			queryId = context.get("_queryId").toString();
		} else {
			throw new ProcessingException(null, "No query id given");
		}

		Object obj = context.get(selectedResultsInput);
		final Collection<SessionPath> selectedResults = new ArrayList<>();
		if(obj != null && obj instanceof Collection) {
			selectedResults.addAll((Collection<SessionPath>)obj);
		}
				
		final QueryManager queryManager = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = queryManager.createResultSetManager();
		final List<Query> projectQueries = rsManager.getQueries(project);
		final String qId = queryId;

		Optional<Query> selectedQuery =
				projectQueries.parallelStream()
					.filter( q -> q.getUUID().toString().equals(qId) )
					.findAny();
		if(selectedQuery.isPresent()) {
			final List<ResultSet> allResultSets = rsManager.getResultSetsForQuery(project, selectedQuery.get())
					.stream()
					.sorted( (rs1, rs2) -> rs1.getSessionPath().compareTo(rs2.getSessionPath()) )
					.collect( Collectors.toList() );
			final List<ResultSet> resultSets = 
					(selectedResults == null || selectedResults.size() == 0
						? allResultSets
						: allResultSets.stream()
								.filter( (rs) -> selectedResults.contains(new SessionPath(rs.getSessionPath())) )
								.collect( Collectors.toList() ) 
					);
			
			// make sure result sets are sorted
			Collections.sort(resultSets, (r1, r2) -> r1.getSessionPath().toString().compareTo(r2.getSessionPath().toString()) );

			context.put(projectOutput, project);
			context.put(queryField, selectedQuery.get());
			context.put(outputField, resultSets.toArray(new ResultSet[0]));
		} else {
			throw new ProcessingException(null, "Unable to find query with id " + queryId + " in project " + project.getName());
		}
	}

}

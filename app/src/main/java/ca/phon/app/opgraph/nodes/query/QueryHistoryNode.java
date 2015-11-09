package ca.phon.app.opgraph.nodes.query;

import java.util.List;
import java.util.Optional;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;

@OpNodeInfo(
		name="Query History",
		description="Load results from query history",
		category="Report"
)
public class QueryHistoryNode extends OpNode {
	
	private InputField projectInput = new InputField("project", "Phon project", true, true, Project.class);
	
	private InputField queryIdInput = new InputField("queryId", "Query history id", true, true, String.class);

	private OutputField projectOutput = new OutputField("project", "Phon project", true, Project.class);
	
	private OutputField queryField = new OutputField("query",
			"Query parameters", true, Query.class);

	private OutputField outputField = new OutputField("result sets", 
			"Result set, one per input record container", true, ResultSet[].class);
	
	public QueryHistoryNode() {
		super();
		
		putField(projectInput);
		putField(queryIdInput);
		
		putField(projectOutput);
		putField(queryField);
		putField(outputField);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		Project project = null;
		if(context.containsKey(projectInput)) {
			project = (Project)context.get(projectInput);
		} else if(context.containsKey("_project") && context.get("_project") instanceof Project) {
			project = (Project)context.get("_project");
		} else {
			throw new ProcessingException(null, "No project given");
		}
		
		String queryId = "";
		if(context.containsKey(queryIdInput)) {
			queryId = (String)context.get(queryIdInput);
		} else if(context.containsKey("_queryId")) {
			queryId = context.get("_queryId").toString();
		} else {
			throw new ProcessingException(null, "No query id given");
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
			
			final List<ResultSet> resultSets = rsManager.getResultSetsForQuery(project, selectedQuery.get());
			
			
			context.put(projectOutput, project);
			context.put(queryField, selectedQuery);
			context.put(outputField, resultSets.toArray(new ResultSet[0]));
		} else {
			throw new ProcessingException(null, "Unable to find query with id " + queryId + " in project " + project.getName());
		}
	}
	
}

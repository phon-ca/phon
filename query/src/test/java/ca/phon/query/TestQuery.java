package ca.phon.query;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ca.phon.project.Project;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryTask;
import ca.phon.session.Session;

/**
 * Test a query with a list of given script parameters
 * 
 */
@RunWith(Parameterized.class)
public class TestQuery {
	
	/**
	 * The script
	 */
	private QueryScript queryScript;
	
	private Project project;
	
	private Session session;
	
	private Map<String, Object> params;
	
	private int expectedResults = 0;
	
	public TestQuery(Project project, Session session, String scriptPath, Map<String, Object> params, int expectedResults) {
		this.project = project;
		this.session = session;
		this.params = params;
		try {
			this.queryScript = new QueryScript(new File(scriptPath));
			setupParams();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.expectedResults = expectedResults;
	}
	
	private void setupParams() {
		for(String key:params.keySet()) {
			queryScript.setScriptParam(key, params.get(key));
		}
	}

	@Test
	public void testQuery() {
		final QueryManager manager = QueryManager.getSharedInstance();
		final QueryFactory factory = manager.createQueryFactory();
		final ResultSet rs = factory.createResultSet();
		
		final QueryTask task = new QueryTask(project, queryScript);
		task.setSession(session);
		task.setResultSet(rs);
		task.run();
		
		Assert.assertEquals(expectedResults, rs.size());
	}
}

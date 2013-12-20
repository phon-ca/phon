package ca.phon.query.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Scriptable;

import ca.phon.project.Project;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.script.QueryScript.QueryFunction;
import ca.phon.script.PhonScriptException;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.worker.PhonTask;

/**
 * Run a query given a project, session and query script.
 * 
 */
public class QueryTask extends PhonTask {
	
	private final static Logger LOGGER = Logger.getLogger(QueryTask.class.getName());
	
	private final Project project;
	
	private final Session session;
	
	private final QueryScript queryScript;
	
	// include excluded records?
	private boolean includeExcludedRecords = false;
	
	/*
	 * Result set
	 */
	private ResultSet resultSet = null;
	
	public QueryTask(Project project, Session session, QueryScript queryScript) {
		super();
		this.project = project;
		this.session = session;
		this.queryScript = queryScript;
		
	}
	
	public Project getProject() {
		return project;
	}

	public Session getSession() {
		return session;
	}

	public QueryScript getQueryScript() {
		return queryScript;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public void setResultSet(ResultSet rs) {
		this.resultSet = rs;
	}
	
	public boolean isIncludeExcludedRecords() {
		return includeExcludedRecords;
	}

	public void setIncludeExcludedRecords(boolean includeExcludedRecords) {
		this.includeExcludedRecords = includeExcludedRecords;
	}

	private ResultSet createResultSet() {
		final QueryManager qm = QueryManager.getSharedInstance();
		final QueryFactory qf = qm.createQueryFactory();
		final ResultSet retVal = qf.createResultSet();
		retVal.setSessionPath(getSession().getCorpus(), getSession().getName());
		return retVal;
	}
	
	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		try {
			final ResultSet rs = executeQuery();
			resultSet = rs;
			setResultSet(rs);
			super.setStatus(TaskStatus.FINISHED);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
		}
	}
	
	/**
	 * Execute the query and return the result set.
	 * 
	 * @return query results
	 */
	public ResultSet executeQuery() 
		throws PhonScriptException {
		final ResultSet rs = createResultSet();
		
		final Session session = getSession();
		final int totalRecords = session.getRecordCount();
		
		// setup script
		final QueryScriptContext ctx = getQueryScript().getQueryContext();
		final Scriptable scope = ctx.getEvaluatedScope();
		ctx.installParams(scope);
		
		// check for query_record - required
		if(!ctx.hasQueryRecord(scope)) {
			throw new PhonScriptException("Script must define the " + QueryFunction.QUERY_RECORD.getName() + " function");
		}
		
		// add result set ass top-level object
		scope.put("results", scope, rs);
		
		final QueryManager qm = QueryManager.getSharedInstance();
		final QueryFactory factory = qm.createQueryFactory();
		scope.put("factory", scope, factory);
		
		// call begin_search
		if(ctx.hasBeginSearch(scope)) {
			ctx.callBeginSearch(scope, session);
		}
		
		// call query record for each record in session
		for(int i = 0; i < totalRecords; i++) {
			final int progress = Math.round(((float)(i+1)/(float)totalRecords) * 100.0f);
			setProperty(PROGRESS_PROP, progress);
			
			final Record record = session.getRecord(i);
			
			boolean includeRecord = !record.isExcludeFromSearches();
			if(!includeRecord && isIncludeExcludedRecords())
				includeRecord = true;
			
			if(includeRecord)
				ctx.callQueryRecord(scope, i, record);
		}
		
		// call end_query
		if(ctx.hasEndSearch(scope)) {
			ctx.callEndSearch(scope, session);
		}
		
		return rs;
	}
}

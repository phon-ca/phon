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
package ca.phon.query.script;

import java.util.*;

import ca.phon.script.params.ScriptParameters;
import org.mozilla.javascript.*;

import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.script.QueryScript.*;
import ca.phon.script.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.util.*;
import ca.phon.worker.*;
import org.mozilla.javascript.tools.debugger.Main;

/**
 * Run a query given a project, session and query script.
 * 
 */
public class QueryTask extends PhonTask {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryTask.class.getName());
	
	private final Project project;
	
	private final Session session;
	
	private final QueryScript queryScript;
	
	private final int serial;
	
	private Iterator<Integer> recordIterable;
	
	// include excluded records?
	private boolean includeExcludedRecords = false;

	/*
	 * Debug session - enable debugging for this session. Both this
	 * flag and the specific record number must be set for debugging
	 * to occur
	 */
	private boolean debugSession = false;

	/*
	 * Debug record, if >= 0 will show debugger when querying this record
	 */
	private int debugRecord = -1;
	
	/*
	 * Result set
	 */
	private ResultSet resultSet = null;
	
	public QueryTask(Project project, Session session, QueryScript queryScript, int serial) {
		super();
		this.project = project;
		this.session = session;
		this.queryScript = queryScript;
		this.serial = serial;
	}
	
	public QueryTask(Project project, Session session, Iterator<Integer> recordIterable, QueryScript queryScript, int serial) {
		super();
		this.project = project;
		this.session = session;
		this.queryScript = queryScript;
		this.serial = serial;
		this.recordIterable = recordIterable;
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

	public int getDebugRecord() {
		return this.debugRecord;
	}

	public void setDebugRecord(int debugRecord) {
		this.debugRecord = debugRecord;
	}

	public boolean isDebugSession() {
		return this.debugSession;
	}

	public void setDebugSession(boolean debugSession) {
		this.debugSession = debugSession;
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
			LOGGER.error( e.getLocalizedMessage(), e);
			super.err = e;
			super.setStatus(TaskStatus.ERROR);
		}
	}

	private void setupScope(Scriptable scope, QueryScriptContext ctx, Project project, int serial, ResultSet rs)
		throws PhonScriptException {
		ctx.installParams(scope);

		scope.put("project", scope, project);
		scope.put("serial", scope, serial);

		scope.put("err", scope, ctx.getStdErr());
		scope.put("out", scope, ctx.getStdOut());

		// add result set ass top-level object
		scope.put("results", scope, rs);

		final QueryManager qm = QueryManager.getSharedInstance();
		final QueryFactory factory = qm.createQueryFactory();
		scope.put("factory", scope, factory);
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
		final int totalRecords = 
				session.getRecordCount();
		
		// setup script
		final QueryScriptContext ctx = getQueryScript().getQueryContext();
		final Scriptable scope = ctx.getEvaluatedScope();
		setupScope(scope, ctx, project, serial, rs);
		
		// check for query_record - required
		if(!ctx.hasQueryRecord(scope)) {
			throw new PhonScriptException("Script must define the " + QueryFunction.QUERY_RECORD.getName() + " function");
		}
		
		// call begin_search
		if(ctx.hasBeginSearch(scope)) {
			ctx.callBeginSearch(scope, session);
		}
		
		Iterator<Integer> recordItr = 
				(this.recordIterable == null ? (new Range(0, totalRecords, true)).iterator() : recordIterable);
		// call query record for each record in session
		while(recordItr.hasNext()) {
			int i = recordItr.next();
			try {
				final float progress = ((float)(i+1)/(float)totalRecords) * 100.0f;
				setProperty(PROGRESS_PROP, progress);
				
				final Record record = session.getRecord(i);

				boolean includeRecord = !record.isExcludeFromSearches();
				if (!includeRecord && isIncludeExcludedRecords())
					includeRecord = true;
				if (includeRecord) {
					if (isDebugSession() && i == getDebugRecord()) {
						// show debugger for this record
						org.mozilla.javascript.tools.debugger.Main debugger = Main.mainEmbedded(getName());
						debugger.setBreakOnEnter(false);
						debugger.setBreakOnExceptions(true);

						ScriptParameters params = ctx.getScriptParameters(scope);

						queryScript.resetContext();
						final QueryScriptContext debugCtx = queryScript.getQueryContext();

						final Context jsctx = debugCtx.enter();
						final Scriptable debugScope = debugCtx.getEvaluatedScope();
						jsctx.setOptimizationLevel(-1);
						debugger.attachTo(jsctx.getFactory());
						debugger.setScope(debugScope);
						ctx.exit();

						final Scriptable runScope = debugCtx.getEvaluatedScope(debugScope);
						setupScope(runScope, debugCtx, project, serial, rs);

						debugger.setExitAction(new Runnable() {

							@Override
							public void run() {
								debugger.detach();
								debugger.setVisible(false);
							}

						});
						// break on entering main query script
						debugger.doBreak();
						debugger.setSize(500, 600);
						debugger.setVisible(true);

						ctx.callQueryRecord(runScope, i, record);
					} else {
						ctx.callQueryRecord(scope, i, record);
					}
				}
			} catch (Exception e) {
				// wrap script exceptions
				throw new PhonScriptException("Error at " + session.getCorpus() + "." + session.getName() + "#" + i, e);
			}
		}
		
		// call end_query
		if(ctx.hasEndSearch(scope)) {
			ctx.callEndSearch(scope, session);
		}
		
		return rs;
	}
}

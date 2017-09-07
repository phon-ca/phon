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
package ca.phon.query.script;

import java.util.Iterator;
import java.util.logging.*;

import org.mozilla.javascript.Scriptable;

import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.script.QueryScript.QueryFunction;
import ca.phon.script.PhonScriptException;
import ca.phon.session.*;
import ca.phon.util.Range;
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
	
	private final int serial;
	
	private Iterator<Integer> recordIterable;
	
	// include excluded records?
	private boolean includeExcludedRecords = false;
	
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
		final int totalRecords = 
				session.getRecordCount();
		
		// setup script
		final QueryScriptContext ctx = getQueryScript().getQueryContext();
		final Scriptable scope = ctx.getEvaluatedScope();
		ctx.installParams(scope);
		
		// check for query_record - required
		if(!ctx.hasQueryRecord(scope)) {
			throw new PhonScriptException("Script must define the " + QueryFunction.QUERY_RECORD.getName() + " function");
		}
		
		scope.put("project", scope, project);
		scope.put("serial", scope, serial);
		
		scope.put("err", scope, ctx.getStdErr());
		scope.put("out", scope, ctx.getStdOut());
		
		// add result set ass top-level object
		scope.put("results", scope, rs);
		
		final QueryManager qm = QueryManager.getSharedInstance();
		final QueryFactory factory = qm.createQueryFactory();
		scope.put("factory", scope, factory);
		
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
				final int progress = Math.round(((float)(i+1)/(float)totalRecords) * 100.0f);
				setProperty(PROGRESS_PROP, progress);
				
				final Record record = session.getRecord(i);
				
				boolean includeRecord = !record.isExcludeFromSearches();
				if(!includeRecord && isIncludeExcludedRecords())
					includeRecord = true;
				
				if(includeRecord)
					ctx.callQueryRecord(scope, i, record);
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

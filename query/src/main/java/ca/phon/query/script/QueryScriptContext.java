/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import org.mozilla.javascript.Scriptable;

import ca.phon.query.script.QueryScript.QueryFunction;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class QueryScriptContext extends PhonScriptContext {

	public QueryScriptContext(QueryScript script) {
		super(script);
	}

	/**
	 * Does the script define a <code>query_record = function(session, record)</code> function
	 * 
	 * @return <code>true</code>
	 *  if a <code>query_record</code> function was found, <code>false</code> otherwise
	 */
	public boolean hasQueryRecord(Scriptable scope) {
		return hasQueryFunction(scope, QueryFunction.QUERY_RECORD);
	}
	
	/**
	 * Call the <code>query_record</code> function.
	 * 
	 * @param record
	 */
	public void callQueryRecord(Scriptable scope, Integer recordIndex, Record record) 
		throws PhonScriptException {
		callQueryFunction(scope, QueryFunction.QUERY_RECORD, recordIndex, record);
	}
	
	/**
	 * Does the script define a <code>begin_search</code> function
	 * 
	 * @return <code>true</code>
	 *  if a <code>begin_search</code> function was found, <code>false</code> otherwise
	 */
	public boolean hasBeginSearch(Scriptable scope) {
		return hasQueryFunction(scope, QueryFunction.BEGIN_SEARCH);
	}
	
	/**
	 * Call the <code>begin_search</code> function
	 * 
	 * @param session
	 */
	public void callBeginSearch(Scriptable scope, Session session) 
		throws PhonScriptException {
		callQueryFunction(scope, QueryFunction.BEGIN_SEARCH, session);
	}
	
	/**
	 * Does the script define a <code>end_search</code> function
	 * 
	 * @return <code>true</code>
	 *  if a <code>end_search</code> function was found, <code>false</code> otherwise
	 */
	public boolean hasEndSearch(Scriptable scope) {
		return hasQueryFunction(scope, QueryFunction.END_SEARCH);
	}
	
	/**
	 * Call the <code>end_search</code> function
	 * 
	 * @param session
	 */
	public void callEndSearch(Scriptable scope, Session session)
		throws PhonScriptException {
		callQueryFunction(scope, QueryFunction.END_SEARCH, session);
	}
	
	/**
	 * Does the script define the given {@link QueryFunction}
	 * 
	 * @param function
	 * @return <code>true</code> if the specified function is defined
	 *  in the script, <code>false</code> otherwise
	 */
	public boolean hasQueryFunction(Scriptable scope, QueryFunction function) {
		return hasFunction(scope, function.getName(), function.getArity());
	}

	/**
	 * Call the specified QueryFunction with the given arguments
	 * 
	 * @param function
	 * @param args
	 * 
	 * @return result of calling function or <code>null</code>
	 * 
	 */
	public Object callQueryFunction(Scriptable scope, QueryFunction function, Object... args) 
		throws PhonScriptException {
		return callFunction(scope, function.getName(), args);
	}
}

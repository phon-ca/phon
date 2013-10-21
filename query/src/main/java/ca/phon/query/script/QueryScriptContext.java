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

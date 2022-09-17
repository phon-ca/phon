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
package ca.phon.query;

import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.Session;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mozilla.javascript.Scriptable;

import java.io.*;
import java.util.*;

/**
 * Test a query with a list of given script parameters
 * 
 */
@RunWith(Parameterized.class)
public class TestQuery {
	
	@Parameters
	public static Collection<Object[]> testData() {
		return new ArrayList<Object[]>();
	}
	
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
			this.queryScript = new QueryScript(new File(scriptPath).toURI().toURL());
			setupParams();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.expectedResults = expectedResults;
	}
	
	private void setupParams() {
		final PhonScriptContext ctx = queryScript.getContext();
		Scriptable scope;
		try {
			scope = ctx.getEvaluatedScope();
			final ScriptParameters scriptParams = ctx.getScriptParameters(scope);
			for(String key:params.keySet()) {
				scriptParams.setParamValue(key, params.get(key));
			}
		} catch (PhonScriptException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testQuery() {
		final QueryTask task = new QueryTask(project, session, queryScript, 1);
		task.run();
		
		final ResultSet rs = task.getResultSet();
		
		Assert.assertEquals(expectedResults, rs.size());
	}
}

/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.query;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mozilla.javascript.Scriptable;

import ca.phon.project.DefaultProjectFactory;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.query.analysis.DefaultQueryAnalysis;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryAnalysisInput;
import ca.phon.query.report.ReportIO;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.script.QueryScript;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.SessionPath;

@RunWith(JUnit4.class)
public class TestBasicAnalysis {
	
	
	private final static String TEST_DESIGN = "src/test/resources/test-design.xml";
	
	private final static String TEST_PROJECT = "src/test/resources/test-project";
	private final static String TEST_CORPUS = "corpus";
	private final static String TEST_SESSION = "session";
	
	private final static String PHONES_SCRIPT = "src/main/resources/ca/phon/query/script/Phones.js";
	
	private final static String REPORT_SCRIPT = 
			"var keySet = queryAnalysisResult.resultSetKeys;\r\n" + 
			"\r\n" + 
			"out.println(\"\\\"Session\\\",\\\"Number of Results\\\"\");\r\n" + 
			"\r\n" +
			"var itr = keySet.iterator();\r\n" +
			"while(itr.hasNext()) {\r\n" + 
			"	var sessionPath = itr.next();\r\n" + 
			"    var resultSet = queryAnalysisResult.getResultSet(sessionPath);\r\n" + 
			"    \r\n" + 
			"    out.println(\"\\\"\" + sessionPath + \"\\\",\\\"\" + resultSet.numberOfResults(true) + \"\\\"\");\r\n" + 
			"}";

	@Test
	public void testReportDesignAnalysis() throws IOException, ProjectConfigurationException {
		final QueryScript qs = getScript();
		final ReportDesign reportDesign = getReportDesign();
		
		final Project project = (new DefaultProjectFactory()).openProject(new File(TEST_PROJECT));
		final SessionPath sp = new SessionPath(TEST_CORPUS, TEST_SESSION);
		final SessionPath[] selectedSessions = new SessionPath[]{ sp };
		
		final QueryAnalysisInput input = new QueryAnalysisInput();
		input.setProject(project);
		input.setSessions(Arrays.asList(selectedSessions));
		
		final QueryAnalysis analysis = new DefaultQueryAnalysis(qs, reportDesign);
		final String result = analysis.performAnalysis(input);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.length() > 0);
	}
	
	@Test
	public void testScriptReportAnalysis() throws IOException, ProjectConfigurationException {
		final QueryScript qs = getScript();
		final PhonScript script = getReportScript();
		
		final Project project = (new DefaultProjectFactory()).openProject(new File(TEST_PROJECT));
		final SessionPath sp = new SessionPath(TEST_CORPUS, TEST_SESSION);
		final SessionPath[] selectedSessions = new SessionPath[]{ sp };
		
		final QueryAnalysisInput input = new QueryAnalysisInput();
		input.setProject(project);
		input.setSessions(Arrays.asList(selectedSessions));
		
		final QueryAnalysis analysis = new DefaultQueryAnalysis(qs, script);
		final String result = analysis.performAnalysis(input);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.length() > 0);
	}
	
	private PhonScript getReportScript() throws IOException {
		PhonScript retVal = new BasicScript(REPORT_SCRIPT);
		return retVal;
	}
	
	private ReportDesign getReportDesign() throws IOException {
		return ReportIO.readDesign(new File(TEST_DESIGN));
	}
	
	private QueryScript getScript() throws IOException {
		final QueryScript retVal = 
				new QueryScript(new File(PHONES_SCRIPT).toURI().toURL());
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("filters.primary.filter", "\\c+");
		params.put("filters.primary.filterType", new EnumScriptParam.ReturnValue("Phonex", 2));
		setupParams(retVal, params);
		return retVal;
	}

	private void setupParams(QueryScript queryScript, Map<String, Object> params) {
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

}

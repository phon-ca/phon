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

import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.query.analysis.DefaultQueryAnalysis;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryAnalysisInput;
import ca.phon.query.analysis.QueryStep;
import ca.phon.query.analysis.ReportDesignStep;
import ca.phon.query.db.QueryManager;
import ca.phon.query.report.ReportIO;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
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

	@Test
	public void testPhonesAnalysis() throws IOException, ProjectConfigurationException {
		final QueryScript qs = getScript();
		final ReportDesign reportDesign = getReportDesign();
		
		final Project project = (new ProjectFactory()).openProject(new File(TEST_PROJECT));
		final SessionPath sp = new SessionPath(TEST_CORPUS, TEST_SESSION);
		final SessionPath[] selectedSessions = new SessionPath[]{ sp };
		
		final QueryAnalysisInput input = new QueryAnalysisInput();
		input.setProject(project);
		input.setQuery(qs);
		input.setReportDesign(reportDesign);
		input.setSessions(Arrays.asList(selectedSessions));
		
		final QueryAnalysis analysis = new DefaultQueryAnalysis();
		final String result = analysis.performAnalysis(input);
		
		Assert.assertNotNull(result);
		Assert.assertTrue(result.length() > 0);
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

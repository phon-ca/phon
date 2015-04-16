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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.query.analysis.QueryAnalysis;
import ca.phon.query.analysis.QueryAnalysisInput;
import ca.phon.query.analysis.QueryStep;
import ca.phon.query.analysis.ScriptReportStep;
import ca.phon.query.script.QueryScript;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.session.SessionPath;

@RunWith(JUnit4.class)
public class TestWordMatchAnalysis {

	private final static String WORDMATCH_SCRIPT = "src/main/resources/ca/phon/query/script/Word Match.js";
	
	private final static String REPORT_SCRIPT = "src/main/resources/ca/phon/query/analysis/simple_report.js";
	
	private final static String TEST_PROJECT = "src/test/resources/test-project";
	private final static String TEST_CORPUS = "corpus";
	private final static String TEST_SESSION = "session";
	
	@Test
	public void testWordMatchAnalysis() throws IOException, ProjectConfigurationException {
		final QueryScript qs = getQueryScript();
		final PhonScript rs = getReportScript();
		
		final QueryStep queryStep = new QueryStep(qs);
		final ScriptReportStep reportStep = new ScriptReportStep(rs);
		final QueryAnalysis qa = new QueryAnalysis(queryStep, reportStep);
		
		final Project project = (new ProjectFactory()).openProject(new File(TEST_PROJECT));
		final SessionPath sp = new SessionPath(TEST_CORPUS, TEST_SESSION);
		final SessionPath[] selectedSessions = new SessionPath[]{ sp };
		
		final QueryAnalysisInput input = new QueryAnalysisInput();
		input.setProject(project);
		input.setSessions(Arrays.asList(selectedSessions));
		
		String result = qa.performAnalysis(input);
		
		System.out.println(result);
	}
	
	private PhonScript getReportScript() throws IOException {
		final BasicScript retVal = new BasicScript(new File(REPORT_SCRIPT));
		return retVal;
	}
	
	private QueryScript getQueryScript() throws IOException {
		final QueryScript retVal = 
				new QueryScript(new File(WORDMATCH_SCRIPT).toURI().toURL());
		return retVal;
	}
	
}

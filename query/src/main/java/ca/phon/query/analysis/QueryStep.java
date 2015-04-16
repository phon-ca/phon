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
package ca.phon.query.analysis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.functor.AnalysisStep;
import ca.phon.project.Project;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryTask;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;

public class QueryStep implements AnalysisStep<QueryAnalysisResult, QueryAnalysisInput> {

	private static final Logger LOGGER = Logger
			.getLogger(QueryStep.class.getName());
	
	private final QueryScript queryScript;
	
	public QueryStep(QueryScript queryScript) {
		this.queryScript = queryScript;
	}
	
	@Override
	public QueryAnalysisResult op(QueryAnalysisInput input) {
		final QueryAnalysisResult result = new QueryAnalysisResult(input);
		final Project project = input.getProject();
		
		int serial = 0;
		for(SessionPath path:input.getSessions()) {
			try {
				final Session session = project.openSession(path.getCorpus(), path.getSession());

				final QueryScript qs = queryScript;
				
				final ByteArrayOutputStream bout = new ByteArrayOutputStream();
				final PrintStream writer = new PrintStream(bout, false, "UTF-8");
				
				qs.getContext().redirectStdErr(writer);
				qs.getContext().redirectStdOut(writer);
				
				final QueryTask qt = new QueryTask(project, session, qs, serial++);
				qt.run();
				
				result.putOutput(path, bout.toString("UTF-8"));
				result.putResultSet(path, qt.getResultSet());
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return result;
	}

	@Override
	public Class<QueryAnalysisResult> getReturnType() {
		return QueryAnalysisResult.class;
	}

	@Override
	public Class<QueryAnalysisInput> getParameterType() {
		return QueryAnalysisInput.class;
	}
	
}

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
package ca.phon.query.analysis;

import ca.phon.functor.AbstractAnalysis;
import ca.phon.functor.AnalysisStep;

public class QueryAnalysis extends AbstractAnalysis<String, QueryAnalysisInput> {
	
	public QueryAnalysis(AnalysisStep<QueryAnalysisResult, QueryAnalysisInput> queryStep,
			AnalysisStep<String, QueryAnalysisResult> reportStep) {
		addStep(queryStep);
		addStep(reportStep);
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public Class<QueryAnalysisInput> getParameterType() {
		return QueryAnalysisInput.class;
	}
	
}

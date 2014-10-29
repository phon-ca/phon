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

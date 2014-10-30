package ca.phon.query.analysis;

import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScript;

public class DefaultQueryAnalysis extends QueryAnalysis {

	public DefaultQueryAnalysis(QueryScript queryScript, ReportDesign reportDesign) {
		super(new QueryStep(queryScript), new ReportDesignStep(reportDesign));
	}
	
	public DefaultQueryAnalysis(QueryScript queryScript, PhonScript reportScript) {
		super(new QueryStep(queryScript), new ScriptReportStep(reportScript));
	}

	public DefaultQueryAnalysis(QueryScript queryScript, String reportScript) {
		super(new QueryStep(queryScript), new ScriptReportStep(reportScript));
	}
	
}

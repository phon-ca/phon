package ca.phon.app.query.analysis.actions;

import ca.phon.ui.CommonModuleFrame;

public class PMLUAction extends AnalysisAction {

	private static final long serialVersionUID = -2704840851738827770L;
	
	private final static String QUERY_SCRIPT = "ca/phon/query/script/PMLU.js";
	
	private final static String REPORT_SCRIPT = "ca/phon/query/analysis/simple_report.js";
	
	private final static String TXT = "PMLU...";
	
	private final static String DESC = "Phonological Mean Length of Utterance";
	
	public PMLUAction(CommonModuleFrame projectFrame) {
		super(projectFrame, QUERY_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
}

package ca.phon.app.query.analysis.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

public class WordMatchAction extends AnalysisAction {

	private static final long serialVersionUID = -5997676379072566324L;

	private static final String TXT = "Word Match...";
	
	private static final String DESC = "Whole word comparasons of IPA Actual vs. IPA Target forms";
	
	private static final String WORDMATCH_SCRIPT = 
			"ca/phon/query/script/Word Match.js";
	
	private static final String REPORT_SCRIPT =
			"ca/phon/query/analysis/simple_report.js";
	
	public WordMatchAction(CommonModuleFrame projectFrame) {
		super(projectFrame, WORDMATCH_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

}

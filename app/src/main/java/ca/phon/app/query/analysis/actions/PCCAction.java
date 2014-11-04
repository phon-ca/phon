package ca.phon.app.query.analysis.actions;

import ca.phon.ui.CommonModuleFrame;

public class PCCAction extends AnalysisAction {

	private static final long serialVersionUID = 160534063316471612L;

	private final static String PCC_SCRIPT = "ca/phon/query/script/PCC-PVC.js";
	
	private final static String REPORT_SCRIPT = "ca/phon/query/analysis/simple_report.js";
	
	private final static String TXT = "PCC-PVC...";
	
	private final static String DESC = "PCC/PVC report...";
	
	public PCCAction(CommonModuleFrame projectFrame) {
		super(projectFrame, PCC_SCRIPT, REPORT_SCRIPT);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
}

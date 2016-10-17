package ca.phon.app.opgraph.wizard.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.wizard.NodeWizard;

public class ReportAction extends HookableAction {

	private final NodeWizard wizard;
	
	private final String reportName;
	
	public ReportAction(NodeWizard wizard, String reportName) {
		super();
		
		this.wizard = wizard;
		this.reportName = reportName;
		
		putValue(NAME, "Create report " + reportName);
		putValue(SHORT_DESCRIPTION, "Generate HTML report " + reportName);
	}	
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		
	}

}

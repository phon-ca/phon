package ca.phon.app.project.git.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.GitCommitWizard;

public class CommitAction extends ProjectWindowAction {

	private static final long serialVersionUID = 8240539097062235081L;

	public CommitAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Commit...");
		putValue(SHORT_DESCRIPTION, "Commit changes");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final GitCommitWizard wizard = new GitCommitWizard(getWindow().getProject());
		wizard.setParentFrame(getWindow());
		wizard.pack();
		wizard.setSize(600, wizard.getHeight());
		wizard.centerWindow();
		wizard.setVisible(true);
	}

}

package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.checkwizard.CheckWizard;

public class CheckTranscriptionsAction extends ProjectWindowAction {
	
	private static final long serialVersionUID = -5819488757671662388L;

	public CheckTranscriptionsAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Check Transcriptions...");
		putValue(SHORT_DESCRIPTION, "Check IPA Transcriptions for project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final CheckWizard cw = new CheckWizard(getWindow().getProject());
		cw.pack();
		cw.setLocationRelativeTo(getWindow());
		cw.setVisible(true);
	}

}

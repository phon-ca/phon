package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.AnonymizeParticipantInfoWizard;
import ca.phon.app.project.ProjectWindow;

public class AnonymizeAction extends ProjectWindowAction {

	private static final long serialVersionUID = 1438208708979568761L;

	public AnonymizeAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Anonymize Participant Information...");
		putValue(SHORT_DESCRIPTION, "Anonymize participant information for project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final AnonymizeParticipantInfoWizard wizard = new AnonymizeParticipantInfoWizard(getWindow().getProject());
		wizard.setSize(500, 600);
		wizard.centerWindow();
		wizard.showWizard();
	}

}

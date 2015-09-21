package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.mergewizard.DeriveSessionWizard;
import ca.phon.ui.CommonModuleFrame;

public class DeriveSessionAction extends ProjectWindowAction {

	private static final long serialVersionUID = 4025880051460438742L;

	public DeriveSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Derive Session...");
		putValue(SHORT_DESCRIPTION, "Create a new session from existing data");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final DeriveSessionWizard wizard = new DeriveSessionWizard(getWindow().getProject());
		wizard.setParentFrame(getWindow());
		wizard.setSize(600, 500);
		wizard.setLocationByPlatform(true);
		wizard.setVisible(true);
	}

}

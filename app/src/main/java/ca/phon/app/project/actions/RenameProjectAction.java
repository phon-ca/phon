package ca.phon.app.project.actions;

import ca.phon.app.project.*;
import ca.phon.project.Project;

import java.awt.event.ActionEvent;

public class RenameProjectAction extends ProjectWindowAction {

	public final static String TXT = "Rename project...";

	public final static String DESC = "Change project name...";

	public RenameProjectAction(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project project = getWindow().getProject();

		final RenameProjectDialog dialog = new RenameProjectDialog(project);
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(getWindow());
		dialog.setVisible(true);
	}

}

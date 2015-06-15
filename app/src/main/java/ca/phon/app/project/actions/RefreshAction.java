package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.project.ProjectWindow;

public class RefreshAction extends ProjectWindowAction {

	private static final long serialVersionUID = 2777437252867377184L;

	public RefreshAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Refresh");
		putValue(SHORT_DESCRIPTION, "Refresh project");
		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		putValue(ACCELERATOR_KEY, ks);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getWindow().refreshProject();
	}

}

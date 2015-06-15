package ca.phon.app.project.git.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;

public class StatusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(StatusAction.class.getName());

	private static final long serialVersionUID = 7618297564321509656L;

	public StatusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Status...");
		putValue(SHORT_DESCRIPTION, "Show status of git repository");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final GitProgressBuffer buffer = new GitProgressBuffer("Status");
		final ProjectGitController gitController = new ProjectGitController(getWindow().getProject());
		final CommonModuleFrame window = buffer.createWindow();
		window.setSize(500, 600);
		window.centerWindow();
		window.setVisible(true);
		
		try(Git git = gitController.open()) {
			gitController.printStatus(buffer.getPrinter());
		} catch (IOException | GitAPIException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}

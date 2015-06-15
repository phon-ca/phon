package ca.phon.app.project.git.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.worker.PhonWorker;

public class PushAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(PushAction.class.getName());

	private static final long serialVersionUID = 8107666582684068017L;
	
	public PushAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Push...");
		putValue(SHORT_DESCRIPTION, "Push changes to remote...");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// create window on EDT
		final GitProgressBuffer buffer = 
				new GitProgressBuffer(getWindow().getProject().getName() + " : Push");
		final CommonModuleFrame window = buffer.createWindow();
		window.setParentFrame(getWindow());
		window.setSize(500, 600);
		window.centerWindow();
		window.setVisible(true);

		Runnable doPush = () -> {
			doPush(buffer);
		};
		PhonWorker.getInstance().invokeLater( doPush );
	}
	
	private void doPush(GitProgressBuffer buffer) {
		final ProjectGitController gitController = new ProjectGitController(getWindow().getProject());
		final PrintWriter printer = buffer.getPrinter();
		try(Git git = gitController.open()) {
			Status status = gitController.status();
			if(status.isClean()) {
				printer.println("Nothing to push");
				printer.flush();
				return;
			}
			Iterable<PushResult> prs = gitController.push(buffer);

			for(PushResult pr:prs) {
				printer.println(pr.getURI());
				printer.println(pr.getRemoteUpdates());
				printer.println(pr.getPeerUserAgent());
				printer.println(pr.getAdvertisedRefs());
				printer.println(pr.getMessages());
				printer.flush();
			}
		} catch (IOException | GitAPIException e) {
			printer.println(e.getLocalizedMessage());
			printer.flush();
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

}

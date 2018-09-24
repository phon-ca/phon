/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.project.git.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.worker.PhonWorker;

public class PushAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PushAction.class.getName());

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

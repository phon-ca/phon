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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.worker.PhonWorker;

public class PullAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PullAction.class.getName());

	private static final long serialVersionUID = 8107666582684068017L;
	
	public PullAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Pull...");
		putValue(SHORT_DESCRIPTION, "Pull changes from remote...");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// create window on EDT
		final GitProgressBuffer buffer = 
				new GitProgressBuffer(getWindow().getProject().getName() + " : Pull");
		final CommonModuleFrame window = buffer.createWindow();
		window.setParentFrame(getWindow());
		window.setSize(500, 600);
		window.centerWindow();
		window.setVisible(true);

		Runnable doPull = () -> {
			doPull(buffer);
		};
		PhonWorker.getInstance().invokeLater( doPull );
	}
	
	private void doPull(GitProgressBuffer buffer) {
		final ProjectGitController gitController = new ProjectGitController(getWindow().getProject());
		final PrintWriter printer = buffer.getPrinter();
		try(Git git = gitController.open()) {
			PullResult pr = gitController.pull(buffer);

			final FetchResult fr = pr.getFetchResult();
			printer.println("From " + fr.getURI());
			for(TrackingRefUpdate ref:pr.getFetchResult().getTrackingRefUpdates()) {
				printer.println(ref);
			}
			
			MergeResult mr = pr.getMergeResult();
			printer.println(mr.getMergeStatus());
			printer.flush();
			
			switch(mr.getMergeStatus()) {
			case ABORTED:
				break;
				
			case ALREADY_UP_TO_DATE:
				break;
				
			case CHECKOUT_CONFLICT:
				break;
				
			case CONFLICTING:
				printer.println("Automatic merge failed; fix conflicts and then commit the result.");
				printer.flush();
				break;
				
			case FAILED:
				break;
				
			case FAST_FORWARD:
				break;
				
			case FAST_FORWARD_SQUASHED:
				break;
				
			case MERGED:
				break;
				
			case MERGED_NOT_COMMITTED:
				break;
				
			case MERGED_SQUASHED:
				break;
				
			case MERGED_SQUASHED_NOT_COMMITTED:
				break;
				
			case NOT_SUPPORTED:
				break;
				
			default:
				break;
			}
			
		} catch (IOException | GitAPIException e) {
			printer.println(e.getLocalizedMessage());
			printer.flush();
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

}

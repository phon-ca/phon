/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

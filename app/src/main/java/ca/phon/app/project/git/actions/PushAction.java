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

import java.awt.event.*;
import java.io.*;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.transport.*;

import ca.phon.app.project.*;
import ca.phon.app.project.actions.*;
import ca.phon.app.project.git.*;
import ca.phon.ui.*;
import ca.phon.worker.*;

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
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

}

/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.nativedialogs.*;
import ca.phon.worker.PhonWorker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Initialize new repository for project.  Will also setup default
 * .gitignore file, add all files to the index, and finally commit
 * all changes.
 */
public class InitAction extends ProjectWindowAction {

	private static final long serialVersionUID = 7839341789844508097L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(InitAction.class.getName());

	public InitAction(ProjectWindow projectWindow) {
		super(projectWindow);

		putValue(NAME, "Initialize Git Repository");
		putValue(SHORT_DESCRIPTION, "Initialize a new git repository for the project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final ProjectGitController gitController = new ProjectGitController(getWindow().getProject());

		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(getWindow());
		props.setTitle("Initialize Git Repository");
		props.setHeader("Initialize Git Repository");
		props.setRunAsync(true);
		props.setOptions(MessageDialogProperties.okOptions);

		Runnable doInit = () -> {
			final String msg =
					"Initializing git repository for project at "
							+ gitController.getRepositoryFolder() +"...";
			if(!gitController.hasGitFolder()) {
				LOGGER.info(msg);
				try(Git git = gitController.init()) {
					LOGGER.info("Setting up default .gitignore");
					gitController.setupDefaultGitIgnore();
					LOGGER.info("Adding all files to index");
					gitController.addToIndex(".");
					LOGGER.info("Creating initial commit");
					gitController.commitAllChanges("Initial commit");

					props.setMessage("Initialized new git repository at "
							+ git.getRepository().getDirectory().getAbsolutePath());
				} catch (IOException | GitAPIException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
					props.setMessage(e.getLocalizedMessage());
				}
			} else {
				props.setMessage(".git folder already exists");
			}
			NativeDialogs.showMessageDialog(props);
		};
		PhonWorker.getInstance().invokeLater(doInit);
	}

}

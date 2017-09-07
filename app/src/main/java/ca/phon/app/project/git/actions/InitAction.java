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
import java.util.logging.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.nativedialogs.*;
import ca.phon.worker.PhonWorker;

/**
 * Initialize new repository for project.  Will also setup default
 * .gitignore file, add all files to the index, and finally commit
 * all changes.
 */
public class InitAction extends ProjectWindowAction {

	private static final long serialVersionUID = 7839341789844508097L;

	private final static Logger LOGGER =
			Logger.getLogger(InitAction.class.getName());

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
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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

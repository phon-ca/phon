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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;

public class StatusAction extends ProjectWindowAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(StatusAction.class.getName());

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
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

}

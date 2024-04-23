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

import ca.phon.app.log.LogUtil;
import ca.phon.app.project.ProjectWindow;
import ca.phon.app.project.actions.ProjectWindowAction;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.ui.CommonModuleFrame;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class StatusAction extends ProjectWindowAction {
	
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
			LogUtil.warning(e);
		}
	}

}

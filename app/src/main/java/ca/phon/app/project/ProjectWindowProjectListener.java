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
package ca.phon.app.project;

import java.awt.*;
import java.io.*;
import java.lang.ref.*;

import ca.phon.app.log.LogUtil;
import ca.phon.project.*;
import ca.phon.project.ProjectEvent.*;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.util.FileUtil;
import org.eclipse.jgit.util.FileUtils;

public class ProjectWindowProjectListener implements ProjectListener {

	private final WeakReference<ProjectWindow> projectWindowRef;

	public ProjectWindowProjectListener(ProjectWindow projectWindow) {
		this.projectWindowRef = new WeakReference<ProjectWindow>(projectWindow);
	}

	public ProjectWindow getProjectWindow() {
		return projectWindowRef.get();
	}

	public Project getProject() {
		return getProjectWindow().getProject();
	}

	@Override
	public void projectStructureChanged(ProjectEvent pe) {
		updateProjectWindow();
	}

	@Override
	public void projectDataChanged(ProjectEvent pe) {
		if(pe.getEventType() == ProjectEventType.PROJECT_MEDIAFOLDER_CHANGED ||
				pe.getEventType() == ProjectEventType.CORPUS_MEDIAFOLDER_CHANGED) {
			updateProjectWindow();
		} else if(pe.getEventType() == ProjectEventType.PROJECT_NAME_CHANGED) {
			handleProjectNameChange();
		}
	}

	private void handleProjectNameChange() {
		File projectFolder = new File(getProject().getLocation());
		String projectName = getProject().getName();

		if(!projectName.equals(projectFolder.getName())) {
			// ask user to rename project folder
			int response = getProjectWindow().showMessageDialog("Rename project folder",
					"Rename project folder as '" + projectName + "'?",
					MessageDialogProperties.yesNoOptions);
			if(response == 0) {
				try {
					File newFolder = new File(projectFolder.getParentFile(), projectName);
					FileUtils.rename(projectFolder, newFolder);

					ChangeProjectLocation changeProjectLocation = getProject().getExtension(ChangeProjectLocation.class);
					if(changeProjectLocation != null) {
						changeProjectLocation.setProjectLocation(newFolder.getAbsolutePath());
					}

					RecentProjects recentProjects = new RecentProjects();
					recentProjects.addToHistory(newFolder);

					getProjectWindow().updateLists();
				} catch (IOException e) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(e);
					getProjectWindow().showMessageDialog("Unable to rename project", e.getLocalizedMessage(),
							MessageDialogProperties.okOptions);
				}
			}
		}
	}

	@Override
	public void projectWriteLocksChanged(ProjectEvent pe) {
		updateProjectWindow();
	}

	// updates the project window
	private void updateProjectWindow() {
		getProjectWindow().updateLists();
	}

}

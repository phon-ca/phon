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
package ca.phon.app.project;

import java.lang.ref.WeakReference;

import ca.phon.project.Project;
import ca.phon.project.ProjectEvent;
import ca.phon.project.ProjectEvent.ProjectEventType;
import ca.phon.project.ProjectListener;

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

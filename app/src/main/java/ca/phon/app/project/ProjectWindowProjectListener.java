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

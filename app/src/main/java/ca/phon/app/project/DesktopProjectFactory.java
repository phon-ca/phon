/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.phon.project.DefaultProjectFactory;
import ca.phon.project.Project;
import ca.phon.project.exceptions.ProjectConfigurationException;

/**
 * Factory for cases where the Desktop paradigm is available.  Desktop projects
 * will move contents to the Recycle Bin/Trash when removing corpora or sessions.
 *
 */
public class DesktopProjectFactory extends DefaultProjectFactory {

	@Override
	public Project openProject(File projectFolder) throws IOException, ProjectConfigurationException {
		// check folder
		if(projectFolder == null) {
			throw new NullPointerException();
		}
		
		if(!projectFolder.exists()) {
			throw new FileNotFoundException(projectFolder.getAbsolutePath());
		}
		
		if(!projectFolder.isDirectory()) {
			throw new IOException("Given file object must be a folder.");
		}
		
		// add project to recent projects history
		final RecentProjectHistory history = new RecentProjectHistory();
		history.addToHistory(projectFolder);
		
		return new DesktopProject(projectFolder);
	}

	@Override
	public Project createProject(File projectFolder) throws IOException {
		super.createProject(projectFolder);
		
		try {
			return new DesktopProject(projectFolder);
		} catch (ProjectConfigurationException e) {
			throw new IOException(e);
		}
	}

}

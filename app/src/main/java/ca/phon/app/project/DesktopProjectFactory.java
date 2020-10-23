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

import java.io.*;

import ca.phon.project.*;
import ca.phon.project.exceptions.*;

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
		final RecentProjects history = new RecentProjects();
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

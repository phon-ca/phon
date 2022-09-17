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
package ca.phon.project;

import ca.phon.project.exceptions.ProjectConfigurationException;

import java.io.*;

/**
 * Default project factory implementation.
 *
 */
public class DefaultProjectFactory implements ProjectFactory {

	/**
	 * Open a project from the specified
	 * folder.
	 * 
	 * @param projectFolder
	 * 
	 * @throws IOException if the given file object is
	 *  not a folder or does not exist
	 * @throws ProjectConfigurationException if the given
	 *  folder is not a phon project
	 */
	public Project openProject(File projectFolder) 
		throws IOException, ProjectConfigurationException {
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
		
		return new LocalProject(projectFolder);
	}
	
	/**
	 * Create  project at the specified folder.  The name of
	 * the project will automatically be set to the value of
	 * the final path element.
	 * 
	 * @param projectFolder
	 * 
	 * @throws IOException if a problem occurs while trying to 
	 *  access/write to the given location
	 */
	public Project createProject(File projectFolder) 
			throws IOException {
		final String projectName = projectFolder.getName();
		if(projectName.length() == 0) 
			throw new IOException("Project name cannot be null");
		
		// create project folder, use exsiting folder if it exists
		if(projectFolder.exists()) {
			if(!projectFolder.isDirectory()) {
				throw new IOException("Cannot create project at " + 
						projectFolder.getAbsolutePath() + ".  File already exists.");
			}
		} else {
			if(!projectFolder.mkdirs()) {
				throw new IOException("Could not create project folder at " + 
						projectFolder.getAbsolutePath() + ".");
			}
		}
				
		try {
			return openProject(projectFolder);
		} catch (ProjectConfigurationException pe) {
			throw new IOException(pe);
		}
	}
	
}

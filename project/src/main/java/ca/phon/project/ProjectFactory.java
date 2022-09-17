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
import java.net.URL;

/**
 * Create projects from {@link URL}s
 */
public interface ProjectFactory {
	
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
			throws IOException, ProjectConfigurationException;
	
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
			throws IOException;
	
}

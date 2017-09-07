/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.project;

import java.io.*;
import java.net.URL;

import ca.phon.project.exceptions.ProjectConfigurationException;

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

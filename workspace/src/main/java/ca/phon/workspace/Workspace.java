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
package ca.phon.workspace;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import ca.phon.project.*;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.util.PrefHelper;

public class Workspace {
	
	private static final Logger LOGGER = Logger.getLogger(Workspace.class
			.getName());
	
	/**
	 * Property for the current workspace folder setting
	 */
	public final static String WORKSPACE_FOLDER = Workspace.class.getName() + ".workspaceFolder";
	
	private final static String WORKSPACE_FOLDER_NAME = "PhonWorkspace";
	
	/**
	 * Folder
	 */
	private final File workspaceFolder;
	
	/**
	 * Get the location of the current user's workspace folder
	 * 
	 * @return the user workspace folder
	 */
	public static File userWorkspaceFolder() {
		final String userWorkspacePath = PrefHelper.get(WORKSPACE_FOLDER, defaultWorkspaceFolder().getAbsolutePath());
		return new File(userWorkspacePath);
	}
	
	/**
	 * Get the user workspace
	 * 
	 * @return workspace
	 */
	public static Workspace userWorkspace() {
		return new Workspace(userWorkspaceFolder());
	}
	
	/**
	 * Set the location of the user's workspace
	 * 
	 * @param workspaceFolder
	 */
	public static void setUserWorkspaceFolder(File workspaceFolder) {
		if(!workspaceFolder.isDirectory()) {
			throw new IllegalArgumentException(workspaceFolder + " is not a folder");
		}
		(new WorkspaceHistory()).addToHistory(workspaceFolder);
		PrefHelper.getUserPreferences().put(WORKSPACE_FOLDER, workspaceFolder.getAbsolutePath());
	}
	
	/**
	 * Get the default location of the user's workspace folder
	 * 
	 * @return default workspace location
	 */
	public static File defaultWorkspaceFolder() {
		final String userPath = System.getProperty("user.home");
		final File userDocs = new File(userPath, "Documents");
		return new File(userDocs, WORKSPACE_FOLDER_NAME);
	}
	
	/**
	 * Constructor
	 */
	public Workspace(File folder) {
		super();
		
		if(!folder.isDirectory()) {
			throw new IllegalArgumentException(folder.getAbsolutePath() + " is not a folder");
		}
		workspaceFolder = folder;
	}

	/**
	 * Get the folder
	 * 
	 * @param workspaceFolder
	 */
	public File getWorkspaceFolder() {
		return this.workspaceFolder;
	}
	
	/**
	 * Get projects located in the workspace folder.
	 * 
	 * @return list of project
	 */
	public List<Project> getProjects() {
		final List<Project> retVal = new ArrayList<Project>();
		// scan workspace folder for projects
		final File workspaceFolder = getWorkspaceFolder();
		
		final ProjectFactory pf = new DefaultProjectFactory();
		for(File workspaceFile:workspaceFolder.listFiles()) {
			if(workspaceFile.isDirectory() 
					&& !workspaceFile.isHidden()
					&& !workspaceFile.getName().startsWith("~")
					&& !workspaceFile.getName().endsWith("~")
					&& !workspaceFile.getName().startsWith("__")
					&& !workspaceFile.getName().equals("backups")) {
				// check to see if we can open the project
				try {
					final Project p = pf.openProject(workspaceFile);
					retVal.add(p);
				} catch (IOException e) {} catch (ProjectConfigurationException e) {
					LOGGER
							.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		
		return retVal;
	}
}

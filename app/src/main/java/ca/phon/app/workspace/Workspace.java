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
package ca.phon.app.workspace;

import java.io.*;
import java.util.*;

import ca.phon.app.project.ProjectDetector;
import ca.phon.project.*;
import ca.phon.project.exceptions.*;
import ca.phon.util.*;

public class Workspace {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(Workspace.class
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
	 * @return File workspaceFolder
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

		final ProjectDetector detector = new ProjectDetector();
		final ProjectFactory pf = new DefaultProjectFactory();
		for(File workspaceFile:workspaceFolder.listFiles()) {
			if(workspaceFile.isDirectory() 
					&& !workspaceFile.isHidden()
					&& !workspaceFile.getName().startsWith("~")
					&& !workspaceFile.getName().endsWith("~")
					&& !workspaceFile.getName().startsWith("__")
					&& !workspaceFile.getName().equals("backups")) {
				if(detector.isPhonProjectFolder(workspaceFile)) {
					try {
						final Project p = pf.openProject(workspaceFile);
						retVal.add(p);
					} catch (IOException | ProjectConfigurationException e) {
						LOGGER.error(e.getLocalizedMessage(), e);
					}
				}
			}
		}
		
		return retVal;
	}
}

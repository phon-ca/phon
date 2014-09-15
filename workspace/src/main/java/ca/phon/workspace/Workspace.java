package ca.phon.workspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.project.LocalProject;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
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
		
		final ProjectFactory pf = new ProjectFactory();
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

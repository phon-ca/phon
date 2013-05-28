package ca.phon.workspace;

import java.io.File;

import ca.phon.util.PrefHelper;

public class Workspace {
	
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
		if(workspaceFolder.isDirectory()) {
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
		final File userHome = new File(userPath);
		return new File(userHome, WORKSPACE_FOLDER_NAME);
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
}

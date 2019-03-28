package ca.phon.app;

/**
 * Actions performed when the application version changes.
 * 
 */
public interface VersionTrigger {
	
	/**
	 * Fired when executing a new version of the application for the first time.
	 * This method may be called when the version increases or 
	 * decreases.
	 * 
	 * @param prevVersion
	 * @param currentVersion
	 */
	public void versionChanged(String prevVersion, String currentVersion);
	
}

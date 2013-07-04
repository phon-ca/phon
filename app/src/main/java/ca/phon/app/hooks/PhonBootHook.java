package ca.phon.app.hooks;

import java.util.List;
import java.util.Map;

import ca.phon.app.BootWindow;
import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * started using the {@link BootWindow}.
 * 
 * These hooks are called just before the application is
 * executed and allow modification of the ProcessBuilder
 * and command.
 * 
 */
public interface PhonBootHook {

	/**
	 * Modify/add to a list of vmoptions for the application.
	 * 
	 * @param vmopts
	 * 
	 */
	public void setupVMOptions(List<String> vmopts);
	
	/**
	 * Modify/add to environment for the application.
	 * 
	 * @param environment
	 */
	public void setupEnvironment(Map<String, String> environment);
	
}
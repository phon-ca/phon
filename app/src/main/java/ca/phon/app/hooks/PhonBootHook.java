package ca.phon.app.hooks;

import java.util.List;

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
	 * Make whatever changes are necessary to the command
	 * or process builder.
	 * 
	 * @param processBuilder
	 * @param cmd
	 * 
	 * @throws PluginException
	 */
	public void modifyBoot(ProcessBuilder pb, List<String> cmd)
		throws PluginException;
	
	
}
package ca.phon.app.hooks;

import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * shutdown.
 * 
 * Hooks are called just before System.exit - after
 * the save modified dialog is presented.
 */
public interface PhonShutdownHook {

	/**
	 * Perform whatever operations are necessary for
	 * plug-in cleanup.
	 * 
	 * @throws PluginException on error.  Excpetions
	 * will be printed to the logger
	 */
	public void shutdown() throws PluginException;
	
}

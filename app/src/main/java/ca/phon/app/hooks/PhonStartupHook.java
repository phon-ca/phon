package ca.phon.app.hooks;

import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * opened.
 * 
 * Hooks are called just before the workspace dialog
 * is present.
 */
public interface PhonStartupHook {

	/**
	 * Perform whatever operations are necessary for
	 * plug-in startup.
	 * 
	 * @throws PluginException on error.  Excpetions
	 * will be printed to the logger
	 */
	public void startup() throws PluginException;
	
}

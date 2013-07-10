package ca.phon.app.hooks;

import ca.phon.app.BootWindow;
import ca.phon.plugin.PluginException;

/**
 * Interface used to perform operations before Phon is
 * opened. This hook will always be called when Phon 
 * starts, unlike {@link PhonBootHook} which is only 
 * called when using the {@link BootWindow}.
 * 
 * {@link PhonStartupHook}s are executed just before
 * the first module is called.  A use case for this class
 * is to change the execution flow of the application.
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

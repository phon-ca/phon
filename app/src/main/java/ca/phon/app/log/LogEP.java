package ca.phon.app.log;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;

/**
 * Displays LogConsole for the system logger.
 *
 */
@PhonPlugin
public class LogEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "Log";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		// display application log
		
	}
	
}

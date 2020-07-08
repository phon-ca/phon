package ca.phon.app.phonex;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(minPhonVersion = "3.1.1" )
public class PhonexDebuggerEP implements IPluginEntryPoint {

	public static String EP_NAME = "PhonexDebugger";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		// TODO
	}

}

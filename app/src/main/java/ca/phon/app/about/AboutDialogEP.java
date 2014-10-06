package ca.phon.app.about;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;

public class AboutDialogEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "Help";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final AboutDialog dialog = new AboutDialog();
		dialog.setSize(600, 500);
		dialog.centerWindow();
		dialog.setVisible(true);
	}

}

package ca.phon.app.log;

import java.util.Map;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name="default")
public class LogViewerEP implements IPluginEntryPoint {
	
	public final static String EP_NAME = "LogViewer";

	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		final LogViewer viewer = new LogViewer();
		viewer.setSize(500, 600);
		viewer.centerWindow();
		viewer.setVisible(true);
	}

}

package ca.phon.app.welcome;

import java.util.Map;

import javax.swing.SwingUtilities;

import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg Hedlund", minPhonVersion="Phon 2.2", version="1")
public class WelcomeWindowEP implements IPluginEntryPoint {

	@Override
	public String getName() {
		return "WelcomeWindow";
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		SwingUtilities.invokeLater( () -> {
			final WelcomeWindow window = new WelcomeWindow();
			window.pack();
			window.centerWindow();
			window.setVisible(true);
		});
	}

}

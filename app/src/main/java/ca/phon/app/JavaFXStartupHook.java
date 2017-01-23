package ca.phon.app;

import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import javafx.application.Platform;

public class JavaFXStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private final static Logger LOGGER = Logger.getLogger(JavaFXStartupHook.class.getName());

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Setting JavaFX Platform implicit exit to false");
		Platform.setImplicitExit(false);
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}

}

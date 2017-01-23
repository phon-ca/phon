package ca.phon.app;

import java.util.logging.Logger;

import ca.phon.app.hooks.PhonShutdownHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import javafx.application.Platform;

public class JavaFXShutdownHook implements PhonShutdownHook, IPluginExtensionPoint<PhonShutdownHook> {

	private final static Logger LOGGER = Logger.getLogger(JavaFXShutdownHook.class.getName());
	
	@Override
	public Class<?> getExtensionType() {
		return PhonShutdownHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonShutdownHook> getFactory() {
		return (args) -> this;
	}

	@Override
	public void shutdown() throws PluginException {
		LOGGER.info("Shutdown JavaFX...");
		Platform.exit();
	}

}

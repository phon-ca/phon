package ca.phon.app;

import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.media.VLCHelper;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginException;

@PhonPlugin
public class VLCInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {
	
	private static final Logger LOGGER = Logger
			.getLogger(VLCInitHook.class.getName());

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Initializing VLC library");
		VLCHelper.checkNativeLibrary(false);
	}

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return factory;
	}
	
	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {
		
		@Override
		public PhonStartupHook createObject(Object... args) {
			return VLCInitHook.this;
		}
		
	};

}

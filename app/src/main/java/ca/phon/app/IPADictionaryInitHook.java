package ca.phon.app;

import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;

public class IPADictionaryInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final Logger LOGGER = Logger
			.getLogger(IPADictionaryInitHook.class.getName());
	
	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return factory;
	}

	@Override
	public void startup() throws PluginException {
		LOGGER.info("Initializing IPA Dictionaries");
		IPADatabaseManager.getInstance();
	}
	
	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {
		
		@Override
		public PhonStartupHook createObject(Object... args) {
			return IPADictionaryInitHook.this;
		}
		
	};

}

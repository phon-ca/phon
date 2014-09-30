package ca.phon.app;

import java.io.File;
import java.util.logging.Logger;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.ipadictionary.impl.IPADatabaseManager;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.PrefHelper;

public class IPADictionaryInitHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	private static final Logger LOGGER = Logger
			.getLogger(IPADictionaryInitHook.class.getName());
	
	private static final String DERBY_LOG_PROP = "derby.stream.error.file";
	
	private static final String DERBY_LOG_LOCATION = 
			PrefHelper.getUserDataFolder() + File.separator + "ipadb.log";
	
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
		System.setProperty(DERBY_LOG_PROP,
				PrefHelper.get(DERBY_LOG_PROP, DERBY_LOG_LOCATION));
		IPADatabaseManager.getInstance();
	}
	
	private final IPluginExtensionFactory<PhonStartupHook> factory = new IPluginExtensionFactory<PhonStartupHook>() {
		
		@Override
		public PhonStartupHook createObject(Object... args) {
			return IPADictionaryInitHook.this;
		}
		
	};

}

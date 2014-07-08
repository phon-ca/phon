package ca.phon.app.autosave;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.util.PrefHelper;

public class AutosaveStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {
		final AutosaveManager manager = AutosaveManager.getInstance();
		final int autosaveInterval = 
				PrefHelper.getInt(AutosaveManager.AUTOSAVE_INTERVAL_PROP, 0);
		manager.setInterval(autosaveInterval * 60);
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
			return AutosaveStartupHook.this;
		}
		
	};

}

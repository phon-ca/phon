package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.plugin.*;

public final class JCefStartupHook implements PhonStartupHook, IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public void startup() throws PluginException {

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

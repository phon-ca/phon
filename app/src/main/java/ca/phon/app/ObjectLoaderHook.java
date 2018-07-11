package ca.phon.app;

import ca.phon.app.hooks.PhonStartupHook;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginException;
import ca.phon.syllabifier.SyllabifierLibrary;

public class ObjectLoaderHook implements PhonStartupHook , IPluginExtensionPoint<PhonStartupHook> {

	@Override
	public Class<?> getExtensionType() {
		return PhonStartupHook.class;
	}

	@Override
	public IPluginExtensionFactory<PhonStartupHook> getFactory() {
		return (args) -> this;
	}

	@Override
	public void startup() throws PluginException {
		// load syllabifiers
		LogUtil.info("Loading syllabifiers");
		SyllabifierLibrary.getInstance().availableSyllabifiers();
	}

}

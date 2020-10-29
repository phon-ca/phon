package ca.phon.query.script.params;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.script.params.*;
import ca.phon.script.params.ui.*;

public class TierListScriptParamComponentFactory implements ScriptParamComponentFactory, IPluginExtensionPoint<ScriptParamComponentFactory> {

	@Override
	public Class<?> getExtensionType() {
		return ScriptParamComponentFactory.class;
	}

	@Override
	public IPluginExtensionFactory<ScriptParamComponentFactory> getFactory() {
		return (args) -> this;
	}

	@Override
	public boolean canCreateScriptParamComponent(ScriptParam scriptParam) {
		return (scriptParam instanceof TierListScriptParam);
	}

	@Override
	public JComponent createScriptParamComponent(ScriptParam scriptParam) {
		TierListScriptParamPanel panel = new TierListScriptParamPanel((TierListScriptParam)scriptParam);
		return panel;
	}
	
}

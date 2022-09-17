package ca.phon.query.script.params;

import ca.phon.plugin.*;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ui.ScriptParamComponentFactory;

import javax.swing.*;

public class TierSelectionScriptParamComponentFactory implements ScriptParamComponentFactory, IPluginExtensionPoint<ScriptParamComponentFactory> {

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
		return (scriptParam instanceof TierSelectionScriptParam);
	}

	@Override
	public JComponent createScriptParamComponent(ScriptParam scriptParam) {
		TierSelectionScriptParamPanel panel = new TierSelectionScriptParamPanel((TierSelectionScriptParam) scriptParam);
		return panel;
	}

}

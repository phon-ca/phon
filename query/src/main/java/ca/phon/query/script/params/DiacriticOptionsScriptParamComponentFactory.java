package ca.phon.query.script.params;

import javax.swing.JComponent;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ui.ScriptParamComponentFactory;

public class DiacriticOptionsScriptParamComponentFactory 
	implements ScriptParamComponentFactory, IPluginExtensionPoint<ScriptParamComponentFactory>{

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
		return (scriptParam instanceof DiacriticOptionsScriptParam);
	}

	@Override
	public JComponent createScriptParamComponent(ScriptParam scriptParam) {
		DiacriticOptionsPanel retVal = new DiacriticOptionsPanel((DiacriticOptionsScriptParam)scriptParam);
		
		
		
		return retVal;
	}
	
}

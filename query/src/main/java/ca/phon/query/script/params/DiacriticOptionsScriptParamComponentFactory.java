package ca.phon.query.script.params;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.script.params.*;
import ca.phon.script.params.ui.*;

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

package ca.phon.query.script.params;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.ipamap2.DiacriticSelector;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ui.ScriptParamComponentFactory;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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

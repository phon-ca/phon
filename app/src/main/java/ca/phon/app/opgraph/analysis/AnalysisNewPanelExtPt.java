package ca.phon.app.opgraph.analysis;

import ca.phon.app.opgraph.editor.NewDialogPanel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Analysis Opgraph Editor Model")
public class AnalysisNewPanelExtPt implements IPluginExtensionPoint<NewDialogPanel> {

	@Override
	public Class<?> getExtensionType() {
		return NewDialogPanel.class;
	}

	@Override
	public IPluginExtensionFactory<NewDialogPanel> getFactory() {
		final IPluginExtensionFactory<NewDialogPanel> factory = (Object ... args) -> {
			return new AnalysisNewPanel();
		};
		return factory;
	}

}

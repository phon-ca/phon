package ca.phon.app.opgraph.report;

import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Report New Dialog Panel")
public class ReportNewPanelExtPt implements IPluginExtensionPoint<NewDialogPanel> {

	@Override
	public Class<?> getExtensionType() {
		return NewDialogPanel.class;
	}

	@Override
	public IPluginExtensionFactory<NewDialogPanel> getFactory() {
		final IPluginExtensionFactory<NewDialogPanel> retVal = (Object ... args) -> {
			return new ReportNewPanel();
		};
		return retVal;
	}

}

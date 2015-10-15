package ca.phon.syllabifier.opgraph.editor;

import ca.phon.opgraph.editor.NewDialogPanel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Syllabifier New Dialog Panel")
public class OpGraphSyllabifierNewDialogPanelExtPt implements IPluginExtensionPoint<NewDialogPanel> {

	@Override
	public Class<?> getExtensionType() {
		return NewDialogPanel.class;
	}

	@Override
	public IPluginExtensionFactory<NewDialogPanel> getFactory() {
		final IPluginExtensionFactory<NewDialogPanel> factory = (Object ... args) -> {
			return new OpGraphSyllabifierNewDialogPanel();
		};
		return factory;
	}

}

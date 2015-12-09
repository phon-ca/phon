package ca.phon.app.opgraph.editor;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;

public class EmptyNewDialogPanelExtPt implements IPluginExtensionPoint<NewDialogPanel> {

	@Override
	public Class<?> getExtensionType() {
		return NewDialogPanel.class;
	}

	@Override
	public IPluginExtensionFactory<NewDialogPanel> getFactory() {
		final IPluginExtensionFactory<NewDialogPanel> factory = (Object ... args) -> {
			return new EmptyNewDialogPanel();
		};
		return factory;
	}

}

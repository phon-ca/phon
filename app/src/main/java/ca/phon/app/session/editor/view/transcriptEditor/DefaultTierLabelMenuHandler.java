package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.menu.MenuBuilder;

public class DefaultTierLabelMenuHandler implements TierLabelMenuHandler, IPluginExtensionPoint<TierLabelMenuHandler> {

    @Override
    public void addMenuItems(MenuBuilder builder) {
        builder.addItem(".", "Testing");
    }

    @Override
    public Class<?> getExtensionType() {
        return TierLabelMenuHandler.class;
    }

    @Override
    public IPluginExtensionFactory<TierLabelMenuHandler> getFactory() {
        return args -> this;
    }
}

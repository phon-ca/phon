package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.menu.MenuBuilder;

public class DefaultTierLabelMenuHandler implements TierLabelMenuHandler, IPluginExtensionPoint<TierLabelMenuHandler> {

    @Override
    public void addMenuItems(MenuBuilder builder) {
        /*var tierView = session.getTierView();
        String tierName = tierLabel.getText().substring(0, tierLabel.getText().length()-1);
        var selectedTierView = tierView
            .stream()
            .filter(tv -> tv.getTierName().equals(tierName))
            .findFirst()
            .orElse(null);
        int tierStartIndex = tierView.indexOf(selectedTierView);

        JMenuItem hideTier = new JMenuItem("Hide tier");
        menu.add(hideTier);
        hideTier.addActionListener(e -> hideTier(tierStartIndex, tierName));

        // If it's not the top tier in a record
        if (tierStartIndex > 0) {
            JMenuItem moveUp = new JMenuItem("Move up");
            menu.add(moveUp);
            moveUp.addActionListener(e -> moveTierUp(tierStartIndex));
        }

        // If it's not the bottom tier in a record
        if (tierStartIndex < tierView.size() - 1) {
            JMenuItem moveDown = new JMenuItem("Move down");
            menu.add(moveDown);
            moveDown.addActionListener(e -> moveTierDown(tierStartIndex));
        }*/
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

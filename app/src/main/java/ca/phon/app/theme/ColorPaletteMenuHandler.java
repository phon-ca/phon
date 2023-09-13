package ca.phon.app.theme;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.IPluginMenuFilter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;

/**
 * Adds color palette designer to tools menu if phon.debug is true
 */
public class ColorPaletteMenuHandler implements IPluginMenuFilter, IPluginExtensionPoint<IPluginMenuFilter> {
    @Override
    public Class<?> getExtensionType() {
        return IPluginMenuFilter.class;
    }

    @Override
    public IPluginExtensionFactory<IPluginMenuFilter> getFactory() {
        return args -> this;
    }

    @Override
    public void filterWindowMenu(Window owner, JMenuBar menu) {
        if(PrefHelper.getBoolean("phon.debug", false)) {
            final MenuBuilder builder = new MenuBuilder(menu);
            final PhonUIAction<Window> showAct = PhonUIAction.consumer(this::showColorPaletteDesigner, owner);
            showAct.putValue(PhonUIAction.NAME, "Color palette designer...");
            showAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show color palette designer");
            builder.addSeparator("Tools/", "color_palette");
            builder.addItem("Tools/", showAct);
        }
    }

    private void showColorPaletteDesigner(Window owner) {
        final CommonModuleFrame cmf = new ColorPaletteWindow();
        cmf.pack();
        if(owner instanceof CommonModuleFrame parent) {
            cmf.setParentFrame(parent);
            cmf.positionRelativeTo(SwingConstants.RIGHT, SwingConstants.LEADING, parent);
        } else {
            cmf.centerWindow();
        }
        cmf.setSize(600, 800);
        cmf.setVisible(true);
    }

}

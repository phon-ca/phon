package ca.phon.app.theme;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Install
 */
public final class PhonUIDefaults implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    public final static String LIGHT_BLUE = "Phon.lightBlue";
    public final static Color DEFAULT_LIGHT_BLUE = UIManager.getColor("Phon.darkBlue");

    public final static String DARK_BLUE = "Phon.darkBlue";
    public final static Color DEFAULT_DARK_BLUE = new Color(17, 75, 122);

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(LIGHT_BLUE, DEFAULT_LIGHT_BLUE);
        defaults.put(DARK_BLUE, DEFAULT_DARK_BLUE);
    }

    @Override
    public Class<?> getExtensionType() {
        return UIDefaults.class;
    }

    @Override
    public IPluginExtensionFactory<UIDefaultsHandler> getFactory() {
        return args -> this;
    }

}

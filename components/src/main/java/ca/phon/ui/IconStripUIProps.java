package ca.phon.ui;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class IconStripUIProps implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    public static final String ICON_STRIP_ICON_COLOR = "IconStrip.iconStripIconColor";
    public static final Color DEFAULT_ICON_STRIP_ICON_COLOR = new Color(0x7f7f7f);

    public static final String ICON_STRIP_ICON_SELECTED_COLOR = "IconStrip.iconStripIconSelectedColor";

    public static final Color DEFAULT_ICON_STRIP_ICON_SELECTED_COLOR = new Color(0x1e88e5);

    public static final String ICON_STRIP_ICON_INACTIVE_COLOR = "IconStrip.iconStripIconInactiveColor";
    public static final Color DEFAULT_ICON_STRIP_ICON_INACTIVE_COLOR = UIManager.getColor("inactiveCaptionText");

    public static final String ICON_STRIP_HOVER_COLOR = "IconStrip.iconStripHoverColor";
    public static final Color DEFAULT_ICON_STRIP_HOVER_COLOR = Color.darkGray;

    public static final String ICON_STRIP_ICON_BACKGROUND = "IconStrip.iconStripIconBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_BACKGROUND = UIManager.getColor("Button.background");

    public static final String ICON_STRIP_ICON_SELECTED_BACKGROUND = "IconStrip.iconStripIconSelectedBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_SELECTED_BACKGROUND = UIManager.getColor("Button.background");

    public static final String ICON_STRIP_ICON_PRESSED_BACKGROUND = "IconStrip.iconStripIconPressedBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_PRESSED_BACKGROUND = new Color(0x9f9f9f);

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(ICON_STRIP_ICON_COLOR, DEFAULT_ICON_STRIP_ICON_COLOR);
        defaults.put(ICON_STRIP_ICON_SELECTED_COLOR, DEFAULT_ICON_STRIP_ICON_SELECTED_COLOR);
        defaults.put(ICON_STRIP_ICON_INACTIVE_COLOR, DEFAULT_ICON_STRIP_ICON_INACTIVE_COLOR);
        defaults.put(ICON_STRIP_HOVER_COLOR, DEFAULT_ICON_STRIP_HOVER_COLOR);
        defaults.put(ICON_STRIP_ICON_BACKGROUND, DEFAULT_ICON_STRIP_ICON_BACKGROUND);
        defaults.put(ICON_STRIP_ICON_SELECTED_BACKGROUND, DEFAULT_ICON_STRIP_ICON_SELECTED_BACKGROUND);
        defaults.put(ICON_STRIP_ICON_PRESSED_BACKGROUND, DEFAULT_ICON_STRIP_ICON_PRESSED_BACKGROUND);
    }

    @Override
    public Class<?> getExtensionType() {
        return UIDefaultsHandler.class;
    }

    @Override
    public IPluginExtensionFactory<UIDefaultsHandler> getFactory() {
        return (args) -> this;
    }
}

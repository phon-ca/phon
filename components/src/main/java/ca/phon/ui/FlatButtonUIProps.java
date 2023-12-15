package ca.phon.ui;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class FlatButtonUIProps implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    public final static String ICON_COLOR_PROP = FlatButtonUIProps.class.getName() + ".iconColor";
    public final static Color DEFAULT_ICON_COLOR = UIManager.getColor("textText");

    public final static String ICON_HOVER_COLOR_PROP = FlatButtonUIProps.class.getName() + ".iconHoverColor";
    public final static Color DEFAULT_ICON_HOVER_COLOR = UIManager.getColor("textText");

    public final static String ICON_PRESSED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".iconPressedColor";
    public final static Color DEFAULT_ICON_PRESSED_COLOR = UIManager.getColor("textText");

    public final static String ICON_DISABLED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".iconDisabledColor";
    public final static Color DEFAULT_ICON_DISABLED_COLOR = UIManager.getColor("textInactiveText");

    public final static String ICON_SELECTED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".iconSelectedColor";
    public final static Color DEFAULT_ICON_SELECTED_COLOR = UIManager.getColor("textText");

    public final static String BG_COLOR_PROP = FlatButtonUIProps.class.getName() + ".bgColor";
    public final Color DEFAULT_BG_COLOR = UIManager.getColor("Button.background");

    public final static String BG_HOVER_COLOR_PROP = FlatButtonUIProps.class.getName() + ".bgHoverColor";
    public final Color DEFAULT_BG_HOVER_COLOR = UIManager.getColor("Button.background");

    public final static String BG_PRESSED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".bgPressedColor";
    public final Color DEFAULT_BG_PRESSED_COLOR = UIManager.getColor("Button.background");

    public final static String BG_DISABLED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".bgDisabledColor";
    public final Color DEFAULT_BG_DISABLED_COLOR = UIManager.getColor("Button.background");

    public final static String BG_SELECTED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".bgSelectedColor";
    public final Color DEFAULT_BG_SELECTED_COLOR = UIManager.getColor("Button.background");

    public final static String BORDER_COLOR_PROP = FlatButtonUIProps.class.getName() + ".borderColor";
    public final Color DEFAULT_BORDER_COLOR = UIManager.getColor("Button.border");

    public final static String BORDER_HOVER_COLOR_PROP = FlatButtonUIProps.class.getName() + ".borderHoverColor";
    public final Color DEFAULT_BORDER_HOVER_COLOR = UIManager.getColor("Button.border");

    public final static String BORDER_PRESSED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".borderPressedColor";
    public final Color DEFAULT_BORDER_PRESSED_COLOR = UIManager.getColor("Button.border");

    public final static String BORDER_DISABLED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".borderDisabledColor";
    public final Color DEFAULT_BORDER_DISABLED_COLOR = UIManager.getColor("Button.border");

    public final static String BORDER_SELECTED_COLOR_PROP = FlatButtonUIProps.class.getName() + ".borderSelectedColor";
    public final Color DEFAULT_BORDER_SELECTED_COLOR = UIManager.getColor("Button.border");

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(ICON_COLOR_PROP, DEFAULT_ICON_COLOR);
        defaults.put(ICON_HOVER_COLOR_PROP, DEFAULT_ICON_HOVER_COLOR);
        defaults.put(ICON_PRESSED_COLOR_PROP, DEFAULT_ICON_PRESSED_COLOR);
        defaults.put(ICON_DISABLED_COLOR_PROP, DEFAULT_ICON_DISABLED_COLOR);
        defaults.put(ICON_SELECTED_COLOR_PROP, DEFAULT_ICON_SELECTED_COLOR);
        defaults.put(BG_COLOR_PROP, DEFAULT_BG_COLOR);
        defaults.put(BG_HOVER_COLOR_PROP, DEFAULT_BG_HOVER_COLOR);
        defaults.put(BG_PRESSED_COLOR_PROP, DEFAULT_BG_PRESSED_COLOR);
        defaults.put(BG_DISABLED_COLOR_PROP, DEFAULT_BG_DISABLED_COLOR);
        defaults.put(BG_SELECTED_COLOR_PROP, DEFAULT_BG_SELECTED_COLOR);
        defaults.put(BORDER_COLOR_PROP, DEFAULT_BORDER_COLOR);
        defaults.put(BORDER_HOVER_COLOR_PROP, DEFAULT_BORDER_HOVER_COLOR);
        defaults.put(BORDER_PRESSED_COLOR_PROP, DEFAULT_BORDER_PRESSED_COLOR);
        defaults.put(BORDER_DISABLED_COLOR_PROP, DEFAULT_BORDER_DISABLED_COLOR);
        defaults.put(BORDER_SELECTED_COLOR_PROP, DEFAULT_BORDER_SELECTED_COLOR);
    }

    @Override
    public Class<?> getExtensionType() {
        return UIDefaultsHandler.class;
    }

    @Override
    public IPluginExtensionFactory<UIDefaultsHandler> getFactory() {
        return args -> this;
    }
}

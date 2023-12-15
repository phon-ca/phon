package ca.phon.app.session.editor;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class SessionEditorUIProps implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    // region view title colours
    public static final String VIEW_ACTIVE_TEXT = "SessionEditor.viewActiveText";
    public static final Color DEFAULT_VIEW_ACTIVE_TEXT = UIManager.getColor("activeCaptionText");

    public static final String VIEW_INACTIVE_TEXT = "SessionEditor.viewInactiveText";
    public static final Color DEFAULT_VIEW_INACTIVE_TEXT = UIManager.getColor("inactiveCaptionText");

    public static final String VIEW_FLAP_ACTIVE_TEXT = "SessionEditor.viewFlapActiveText";
    public static final Color DEFAULT_VIEW_FLAP_ACTIVE_TEXT = UIManager.getColor("activeCaptionText");

    public static final String VIEW_TITLE_FLAP_INACTIVE_TEXT = "SessionEditor.viewFlapInactiveText";
    public static final Color DEFAULT_VIEW_FLAP_INACTIVE_TEXT = UIManager.getColor("inactiveCaptionText");

    public static final String VIEW_FLAP_ACTIVE_RIGHT = "SessionEditor.viewFlapActiveRight";
    public static final Color DEFAULT_VIEW_FLAP_ACTIVE_RIGHT = UIManager.getColor("activeCaption");

    public static final String VIEW_ACTIVE_LEFT = "SessionEditor.viewActiveLeft";
    public static final Color DEFAULT_VIEW_ACTIVE_LEFT = UIManager.getColor("activeCaption");

    public static final String VIEW_INACTIVE_LEFT = "SessionEditor.viewInactiveLeft";
    public static final Color DEFAULT_VIEW_INACTIVE_LEFT = UIManager.getColor("Button.background");

    public static final String VIEW_ACTIVE_RIGHT = "SessionEditor.viewActiveRight";
    public static final Color DEFAULT_VIEW_ACTIVE_RIGHT = UIManager.getColor("activeCaption");

    public static final String VIEW_INACTIVE_RIGHT = "SessionEditor.viewInactiveRight";
    public static final Color DEFAULT_VIEW_INACTIVE_RIGHT = UIManager.getColor("Button.background");

    public static final String VIEW_FLAP_ACTIVE_LEFT = "SessionEditor.viewFlapActiveLeft";
    public static final Color DEFAULT_VIEW_FLAP_ACTIVE_LEFT = UIManager.getColor("activeCaption");

    public static final String VIEW_FLAP_INACTIVE_RIGHT = "SessionEditor.viewFlapInactiveRight";
    public static final Color DEFAULT_VIEW_FLAP_INACTIVE_RIGHT = UIManager.getColor("Button.background");

    public static final String VIEW_FLAP_INACTIVE_LEFT = "SessionEditor.viewFlapInactiveLeft";
    public static final Color DEFAULT_VIEW_FLAP_INACTIVE_LEFT = UIManager.getColor("Button.background");
    // endregion

    // region icon strip colours
    public static final String ICON_STRIP_ICON_COLOR = "SessionEditor.iconStripIconColor";
    public static final Color DEFAULT_ICON_STRIP_ICON_COLOR = new Color(0x7f7f7f);

    public static final String ICON_STRIP_ICON_SELECTED_COLOR = "SessionEditor.iconStripIconSelectedColor";

    public static final Color DEFAULT_ICON_STRIP_ICON_SELECTED_COLOR = new Color(0x1e88e5);

    public static final String ICON_STRIP_ICON_INACTIVE_COLOR = "SessionEditor.iconStripIconInactiveColor";
    public static final Color DEFAULT_ICON_STRIP_ICON_INACTIVE_COLOR = UIManager.getColor("inactiveCaptionText");

    public static final String ICON_STRIP_HOVER_COLOR = "SessionEditor.iconStripHoverColor";
    public static final Color DEFAULT_ICON_STRIP_HOVER_COLOR = Color.darkGray;

    public static final String ICON_STRIP_ICON_BACKGROUND = "SessionEditor.iconStripIconBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_BACKGROUND = UIManager.getColor("Button.background");

    public static final String ICON_STRIP_ICON_SELECTED_BACKGROUND = "SessionEditor.iconStripIconSelectedBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_SELECTED_BACKGROUND = UIManager.getColor("Button.background");

    public static final String ICON_STRIP_ICON_PRESSED_BACKGROUND = "SessionEditor.iconStripIconPressedBackground";
    public static final Color DEFAULT_ICON_STRIP_ICON_PRESSED_BACKGROUND = new Color(0x9f9f9f);
    // endregion

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(VIEW_ACTIVE_TEXT, DEFAULT_VIEW_ACTIVE_TEXT);
        defaults.put(VIEW_INACTIVE_TEXT, DEFAULT_VIEW_INACTIVE_TEXT);
        defaults.put(VIEW_FLAP_ACTIVE_TEXT, DEFAULT_VIEW_FLAP_ACTIVE_TEXT);
        defaults.put(VIEW_TITLE_FLAP_INACTIVE_TEXT, DEFAULT_VIEW_FLAP_INACTIVE_TEXT);
        defaults.put(VIEW_FLAP_ACTIVE_RIGHT, DEFAULT_VIEW_FLAP_ACTIVE_RIGHT);
        defaults.put(VIEW_FLAP_ACTIVE_LEFT, DEFAULT_VIEW_FLAP_ACTIVE_LEFT);
        defaults.put(VIEW_FLAP_INACTIVE_RIGHT, DEFAULT_VIEW_FLAP_INACTIVE_RIGHT);
        defaults.put(VIEW_FLAP_INACTIVE_LEFT, DEFAULT_VIEW_FLAP_INACTIVE_LEFT);
        defaults.put(VIEW_ACTIVE_RIGHT, DEFAULT_VIEW_ACTIVE_RIGHT);
        defaults.put(VIEW_ACTIVE_LEFT, DEFAULT_VIEW_ACTIVE_LEFT);
        defaults.put(VIEW_INACTIVE_RIGHT, DEFAULT_VIEW_INACTIVE_RIGHT);
        defaults.put(VIEW_INACTIVE_LEFT, DEFAULT_VIEW_INACTIVE_LEFT);
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
        return args -> this;
    }
}

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
    public static final Color DEFAULT_VIEW_INACTIVE_LEFT = UIManager.getColor("inactiveCaption");

    public static final String VIEW_ACTIVE_RIGHT = "SessionEditor.viewActiveRight";
    public static final Color DEFAULT_VIEW_ACTIVE_RIGHT = UIManager.getColor("activeCaption");

    public static final String VIEW_INACTIVE_RIGHT = "SessionEditor.viewInactiveRight";
    public static final Color DEFAULT_VIEW_INACTIVE_RIGHT = UIManager.getColor("inactiveCaption");

    public static final String VIEW_FLAP_ACTIVE_LEFT = "SessionEditor.viewFlapActiveLeft";
    public static final Color DEFAULT_VIEW_FLAP_ACTIVE_LEFT = UIManager.getColor("activeCaption");

    public static final String VIEW_FLAP_INACTIVE_RIGHT = "SessionEditor.viewFlapInactiveRight";
    public static final Color DEFAULT_VIEW_FLAP_INACTIVE_RIGHT = UIManager.getColor("inactiveCaption");

    public static final String VIEW_FLAP_INACTIVE_LEFT = "SessionEditor.viewFlapInactiveLeft";
    public static final Color DEFAULT_VIEW_FLAP_INACTIVE_LEFT = UIManager.getColor("inactiveCaption");
    // end region

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

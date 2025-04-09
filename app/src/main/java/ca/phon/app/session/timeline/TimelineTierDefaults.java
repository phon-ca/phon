package ca.phon.app.session.timeline;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class TimelineTierDefaults implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    public final static String TIMELINE_TIER_BACKGROUND = "TimelineTier.background";
    public final static Color DEFAULT_TIMELINE_TIER_BACKGROUND = UIManager.getColor("text");

    public final static String TIMELINE_TIER_FOREGROUND = "TimelineTier.foreground";
    public final static Color DEFAULT_TIMELINE_TIER_FOREGROUND = UIManager.getColor("textText");

    public final static String TIMELINE_TIER_TITLE_FOREGROUND = "TimelineTier.titleForeground";
    public final static Color DEFAULT_TIMELINE_TIER_TITLE_FOREGROUND = Color.blue;

    public final static String TIMELINE_TIER_INTERVAL_BORDER = "TimelineTier.intervalBorder";
    public final static Color DEFAULT_TIMELINE_TIER_INTERVAL_BORDER = Color.lightGray;

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(TIMELINE_TIER_BACKGROUND, DEFAULT_TIMELINE_TIER_BACKGROUND);
        defaults.put(TIMELINE_TIER_FOREGROUND, DEFAULT_TIMELINE_TIER_FOREGROUND);
        defaults.put(TIMELINE_TIER_TITLE_FOREGROUND, DEFAULT_TIMELINE_TIER_TITLE_FOREGROUND);
        defaults.put(TIMELINE_TIER_INTERVAL_BORDER, DEFAULT_TIMELINE_TIER_INTERVAL_BORDER);
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

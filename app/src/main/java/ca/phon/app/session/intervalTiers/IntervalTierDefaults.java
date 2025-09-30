package ca.phon.app.session.intervalTiers;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.theme.UIDefaults;
import ca.phon.ui.theme.UIDefaultsHandler;

import javax.swing.*;
import java.awt.*;

public class IntervalTierDefaults implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {

    public final static String TIMELINE_TIER_BACKGROUND = "IntervalTier.background";
    public final static Color DEFAULT_TIMELINE_TIER_BACKGROUND = UIManager.getColor("text");

    public final static String TIMELINE_TIER_SELECTED_BACKGROUND = "IntervalTier.selectedBackground";
    public final static Color DEFAULT_TIMELINE_TIER_SELECTED_BACKGROUND = UIManager.getColor("textHighlight");

    public final static String TIMELINE_TIER_FOREGROUND = "IntervalTier.foreground";
    public final static Color DEFAULT_TIMELINE_TIER_FOREGROUND = UIManager.getColor("textText");

    public final static String TIMELINE_TIER_TITLE_FOREGROUND = "IntervalTier.titleForeground";
    public final static Color DEFAULT_TIMELINE_TIER_TITLE_FOREGROUND = Color.blue;

    public final static String TIMELINE_TIER_INTERVAL_BORDER = "IntervalTier.intervalBorder";
    public final static Color DEFAULT_TIMELINE_TIER_INTERVAL_BORDER = Color.lightGray;

    public final static String TIMELINE_TIER_SELECTED_INTERVAL_BORDER = "IntervalTier.selectedIntervalBorder";
    public final static Color DEFAULT_TIMELINE_TIER_SELECTED_INTERVAL_BORDER = Color.gray;

    public final static String INTERVAL_HIGHLIGHTER_COLOR = "Interval.highlighterColor";
    public final static Color DEFAULT_INTERVAL_HIGHLIGHTER_COLOR = new Color(0, 187, 255, 128);

    @Override
    public void setupDefaults(UIDefaults defaults) {
        defaults.put(TIMELINE_TIER_BACKGROUND, DEFAULT_TIMELINE_TIER_BACKGROUND);
        defaults.put(TIMELINE_TIER_SELECTED_BACKGROUND, DEFAULT_TIMELINE_TIER_SELECTED_BACKGROUND);
        defaults.put(TIMELINE_TIER_FOREGROUND, DEFAULT_TIMELINE_TIER_FOREGROUND);
        defaults.put(TIMELINE_TIER_TITLE_FOREGROUND, DEFAULT_TIMELINE_TIER_TITLE_FOREGROUND);
        defaults.put(TIMELINE_TIER_INTERVAL_BORDER, DEFAULT_TIMELINE_TIER_INTERVAL_BORDER);
        defaults.put(TIMELINE_TIER_SELECTED_INTERVAL_BORDER, DEFAULT_TIMELINE_TIER_SELECTED_INTERVAL_BORDER);
        defaults.put(INTERVAL_HIGHLIGHTER_COLOR, DEFAULT_INTERVAL_HIGHLIGHTER_COLOR);
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

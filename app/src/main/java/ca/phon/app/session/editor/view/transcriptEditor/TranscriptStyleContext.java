package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.ui.fonts.FontPreferences;

import javax.swing.text.*;
import java.awt.*;

/**
 * A collection of named styles for TranscriptDocument
 */
public class TranscriptStyleContext extends StyleContext {

    public final static String DEFAULT = "default";

    public final static String TIER_NAME = "tierName";

    public final static String TIER_CONTENT = "tierContent";

    public final static String GHOST_TEXT = "ghostText";

    public TranscriptStyleContext() {
        super();

        // add default styles
        addDefaultStyles();
    }

    private void addDefaultStyles() {
        createDefaultStyle();
        createTierNameStyle();
        createTierContentStyle();
        createGhostTextStyle();
    }

    private Style createDefaultStyle() {
        final Style style = addStyle(DEFAULT, null);
        StyleConstants.setFontFamily(style, FontPreferences.getTierFont().getFamily());
        StyleConstants.setFontSize(style, FontPreferences.getTierFont().getSize());
        return style;
    }

    private Style createTierNameStyle() {
        final Style style = addStyle(TIER_NAME, getStyle(DEFAULT));
        StyleConstants.setBold(style, true);
        return style;
    }

    private Style createTierContentStyle() {
    	final Style style = addStyle(TIER_CONTENT, getStyle(DEFAULT));
    	return style;
    }

    private Style createGhostTextStyle() {
    	final Style style = addStyle(GHOST_TEXT, getStyle(DEFAULT));
    	StyleConstants.setForeground(style, Color.gray);
    	StyleConstants.setItalic(style, true);
    	return style;
    }

}

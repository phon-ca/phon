package ca.phon.app.session.editor.view.transcriptEditor;

import ca.phon.app.theme.UIDefaults;
import ca.phon.app.theme.UIDefaultsHandler;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.ui.PhonGuiConstants;

import javax.swing.*;
import java.awt.*;

public class TranscriptEditorUIProps implements UIDefaultsHandler, IPluginExtensionPoint<UIDefaultsHandler> {
    public final static String BACKGROUND = "TranscriptEditor.background";
    public final static Color DEFAULT_BACKGROUND = UIManager.getColor("EditorPane.background");

    public final static String FOREGROUND = "TranscriptEditor.foreground";
    public final static Color DEFAULT_FOREGROUND = Color.BLACK;

    public final static String LABEL_BACKGROUND = "TranscriptEditor.labelBackground";
    public final static Color DEFAULT_LABEL_BACKGROUND = PhonGuiConstants.PHON_UI_STRIP_COLOR;

    public final static String SEGMENT_DASH = "TranscriptEditor.segmentDash";
    public final static Color DEFAULT_SEGMENT_DASH = Color.GRAY;

    public final static String TIER_COMMENT = "TranscriptEditor.tierComment";
    public final static Color DEFAULT_TIER_COMMENT = Color.GRAY;

    public final static String INTERNAL_MEDIA = "TranscriptEditor.internalMedia";
    public final static Color DEFAULT_INTERNAL_MEDIA = Color.GRAY;

    public final static String IPA_WORD = "TranscriptEditor.ipaWords";
    public final static Color DEFAULT_IPA_WORD = Color.BLACK;

    public final static String IPA_PAUSE = "TranscriptEditor.ipaPause";
    public final static Color DEFAULT_IPA_PAUSE = Color.GRAY;

    public final static String CLICKABLE_HOVER_UNDERLINE = "TranscriptEditor.clickableHoverUnderline";
    public final static Color DEFAULT_CLICKABLE_HOVER_UNDERLINE = Color.BLACK;

    public final static String SEPARATOR_LINE = "TranscriptEditor.separatorLine";
    public final static Color DEFAULT_SEPARATOR_LINE = Color.GRAY;

    @Override
    public void setupDefaults(UIDefaults uiDefaults) {
        uiDefaults.put(BACKGROUND, DEFAULT_BACKGROUND);
        uiDefaults.put(FOREGROUND, DEFAULT_FOREGROUND);
        uiDefaults.put(LABEL_BACKGROUND, DEFAULT_LABEL_BACKGROUND);
        uiDefaults.put(SEGMENT_DASH, DEFAULT_SEGMENT_DASH);
        uiDefaults.put(TIER_COMMENT, DEFAULT_TIER_COMMENT);
        uiDefaults.put(INTERNAL_MEDIA, DEFAULT_INTERNAL_MEDIA);
        uiDefaults.put(IPA_WORD, DEFAULT_IPA_WORD);
        uiDefaults.put(IPA_PAUSE, DEFAULT_IPA_PAUSE);
        uiDefaults.put(CLICKABLE_HOVER_UNDERLINE, DEFAULT_CLICKABLE_HOVER_UNDERLINE);
        uiDefaults.put(SEPARATOR_LINE, DEFAULT_SEPARATOR_LINE);
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

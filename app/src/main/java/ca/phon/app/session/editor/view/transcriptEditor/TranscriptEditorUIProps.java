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

    public final static String LABEL_BACKGROUND = "TranscriptEditor.labelBackground";
    public final static Color DEFAULT_LABEL_BACKGROUND = PhonGuiConstants.PHON_UI_STRIP_COLOR;

    @Override
    public void setupDefaults(UIDefaults uiDefaults) {
        uiDefaults.put(BACKGROUND, DEFAULT_BACKGROUND);
        uiDefaults.put(LABEL_BACKGROUND, DEFAULT_LABEL_BACKGROUND);
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

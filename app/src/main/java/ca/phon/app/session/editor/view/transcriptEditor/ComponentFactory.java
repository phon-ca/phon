package ca.phon.app.session.editor.view.transcriptEditor;

import javax.swing.*;
import javax.swing.text.AttributeSet;

@FunctionalInterface
public interface ComponentFactory {
    public JComponent createComponent(AttributeSet attrs);
}

package ca.phon.app.session.editor.view.transcript;

import javax.swing.*;
import javax.swing.text.AttributeSet;

@FunctionalInterface
public interface ComponentFactory {
    public JComponent createComponent(AttributeSet attrs);
}

package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleSyllabificationIsComponent extends TranscriptAction {
    private static final long serialVersionUID = -6337537839656747666L;

    public ToggleSyllabificationIsComponent(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean syllabificationIsComponent = view.isSyllabificationComponent();
        putValue(NAME, "Show syllabification as " + (syllabificationIsComponent ? "text" : "component"));
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleSyllabificationIsComponent();
    }
}

package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleSyllabificationVisibleAction extends TranscriptAction {
    private static final long serialVersionUID = -6339597839656747666L;

    public ToggleSyllabificationVisibleAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean syllabificationVisible = view.isSyllabificationVisible();
        putValue(NAME, (syllabificationVisible ? "Hide" : "Show") + " syllabification");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleSyllabificationVisible();
    }
}

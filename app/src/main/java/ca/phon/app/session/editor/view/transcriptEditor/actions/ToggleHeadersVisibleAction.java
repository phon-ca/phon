package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleHeadersVisibleAction extends TranscriptAction {
    private static final long serialVersionUID = -6339057839699747666L;

    public ToggleHeadersVisibleAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean headersVisible = view.isHeadersVisible();
        putValue(NAME, (headersVisible ? "Hide" : "Show") + " header tiers");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleHeadersVisible();
    }
}

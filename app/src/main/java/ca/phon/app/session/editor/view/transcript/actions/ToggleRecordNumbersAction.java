package ca.phon.app.session.editor.view.transcript.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleRecordNumbersAction extends TranscriptAction {
    private static final long serialVersionUID = -6339057839699947666L;

    public ToggleRecordNumbersAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean showingRecordNumbers = view.getShowRecordNumbers();
        putValue(NAME, (showingRecordNumbers ? "Hide" : "Show") + " record numbers");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleShowRecordNumbers();
    }
}

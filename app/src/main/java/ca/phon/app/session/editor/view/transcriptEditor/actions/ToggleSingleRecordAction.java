package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleSingleRecordAction extends TranscriptAction {
    private static final long serialVersionUID = -6339057839610947666L;

    public ToggleSingleRecordAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean singleRecordActivated = view.isSingleRecordActive();
        putValue(NAME, "Show " + (singleRecordActivated ? "all records" : "single record"));
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleSingleRecordActive();
    }
}

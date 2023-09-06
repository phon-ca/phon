package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleLabelsVisibleAction extends TranscriptAction {
    private static final long serialVersionUID = -6339057839656747666L;

    public ToggleLabelsVisibleAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean labelsVisible = view.getLabelsVisible();
        putValue(NAME, (labelsVisible ? "Hide" : "Show") + " labels");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleLabelsVisible();
    }
}

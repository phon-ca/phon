package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleAlignmentIsComponentAction extends TranscriptAction {
    private static final long serialVersionUID = -6337537839656747666L;

    public ToggleAlignmentIsComponentAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean alignmentIsComponent = view.isAlignmentComponent();
        putValue(NAME, "Show alignment as " + (alignmentIsComponent ? "text" : "component"));
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleAlignmentIsComponent();
    }
}

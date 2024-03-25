package ca.phon.app.session.editor.view.transcript.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleAlignmentIsComponentViewAction extends TranscriptViewAction {
    private static final long serialVersionUID = -6337537839656747666L;

    public ToggleAlignmentIsComponentViewAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean alignmentIsComponent = view.isAlignmentComponent();
        putValue(NAME, "Show alignment as " + (alignmentIsComponent ? "text" : "component"));
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleAlignmentIsComponent();
    }
}

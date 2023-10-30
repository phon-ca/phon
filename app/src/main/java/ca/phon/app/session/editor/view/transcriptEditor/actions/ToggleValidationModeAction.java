package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleValidationModeAction extends TranscriptAction {
    private static final long serialVersionUID = -6339597839656747666L;

    public ToggleValidationModeAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean validationModeEnabled = view.isValidationMode();
        putValue(NAME, (validationModeEnabled ? "Disable" : "Enable") + " validation mode");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleValidationMode();
    }
}

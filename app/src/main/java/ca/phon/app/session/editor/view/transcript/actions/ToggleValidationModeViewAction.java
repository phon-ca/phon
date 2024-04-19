package ca.phon.app.session.editor.view.transcript.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.transcript.TranscriptView;

import java.awt.event.ActionEvent;

public class ToggleValidationModeViewAction extends TranscriptViewAction {
    private static final long serialVersionUID = -6339597839656747666L;

    public ToggleValidationModeViewAction(SessionEditor editor, TranscriptView view) {
        super(editor, view);

        final boolean validationModeEnabled = view.isValidationMode();
        putValue(NAME, (validationModeEnabled ? "Disable" : "Enable") + " validation mode");
    }

    @Override
    public void hookableActionPerformed(ActionEvent e) {
        this.getView().toggleValidationMode();
    }
}
package ca.phon.app.session.editor.view.transcript.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.transcript.TranscriptView;

public abstract class TranscriptViewAction extends SessionEditorAction {

    private static final long serialVersionUID = -8937878763351391430L;

    private TranscriptView view;

    public TranscriptViewAction(SessionEditor editor, TranscriptView view) {
        super(editor);
        this.view = view;
    }

    public TranscriptView getView() {
        return this.view;
    }
}

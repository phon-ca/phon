package ca.phon.app.session.editor.view.transcriptEditor.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.transcriptEditor.TranscriptView;

public abstract class TranscriptAction extends SessionEditorAction {

    private static final long serialVersionUID = -8937878763351391430L;

    private TranscriptView view;

    public TranscriptAction(SessionEditor editor, TranscriptView view) {
        super(editor);
        this.view = view;
    }

    public TranscriptView getView() {
        return this.view;
    }
}

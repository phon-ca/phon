package ca.phon.app.session.editor.view.session_information.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;

public abstract class SessionInfoAction extends SessionEditorAction {
	
	private static final long serialVersionUID = 1842274536935284100L;

	private final SessionInfoEditorView view;

	public SessionInfoAction(SessionEditor editor, SessionInfoEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public SessionInfoEditorView getView() {
		return this.view;
	}

}

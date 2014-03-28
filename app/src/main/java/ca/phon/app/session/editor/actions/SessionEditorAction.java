package ca.phon.app.session.editor.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.SessionEditor;

public abstract class SessionEditorAction extends AbstractAction {

	private final SessionEditor editor;
	
	public SessionEditorAction(SessionEditor editor) {
		super();
		this.editor = editor;
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
}

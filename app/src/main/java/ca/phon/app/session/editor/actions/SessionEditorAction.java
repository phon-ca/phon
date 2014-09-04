package ca.phon.app.session.editor.actions;

import javax.swing.Action;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.SessionEditor;

/**
 * Base class for {@link SessionEditor} {@link Action}s.
 */
public abstract class SessionEditorAction extends HookableAction {

	private static final long serialVersionUID = -6639660567310323736L;

	private final SessionEditor editor;
	
	public SessionEditorAction(SessionEditor editor) {
		super();
		this.editor = editor;
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
}

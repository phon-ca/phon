package ca.phon.app.session.editor.actions;

import java.lang.ref.WeakReference;

import javax.swing.Action;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.SessionEditor;

/**
 * Base class for {@link SessionEditor} {@link Action}s.
 */
public abstract class SessionEditorAction extends HookableAction {

	private static final long serialVersionUID = -6639660567310323736L;

	private final WeakReference<SessionEditor> editorRef;
	
	public SessionEditorAction(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
	}
	
	public SessionEditor getEditor() {
		return this.editorRef.get();
	}
	
}

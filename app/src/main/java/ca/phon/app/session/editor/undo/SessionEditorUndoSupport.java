package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import ca.phon.app.session.editor.SessionEditor;

/**
 * Undo support for the {@link SessionEditor}
 *
 */
public class SessionEditorUndoSupport extends UndoableEditSupport {
	
	private final WeakReference<SessionEditor> editorRef;
	
	public SessionEditorUndoSupport(SessionEditor sessionEditor) {
		editorRef = new WeakReference<SessionEditor>(sessionEditor);
	}

	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	@Override
	public synchronized void postEdit(UndoableEdit e) {
		if(e instanceof SessionEditorUndoableEdit) {
			final SessionEditorUndoableEdit edit = SessionEditorUndoableEdit.class.cast(e);
			
			edit.putExtension(Integer.class, getEditor().getCurrentRecordIndex());
			
			edit.doIt();
		}
		super.postEdit(e);
	}
	
}

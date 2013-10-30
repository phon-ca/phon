package ca.phon.app.session.editor.undo;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import ca.phon.app.session.editor.SessionEditor;

/**
 * Undo support for the {@link SessionEditor}
 *
 */
public class SessionEditorUndoSupport extends UndoableEditSupport {

	@Override
	public synchronized void postEdit(UndoableEdit e) {
		if(e instanceof SessionEditorUndoableEdit) {
			final SessionEditorUndoableEdit edit = SessionEditorUndoableEdit.class.cast(e);
			edit.doIt();
		}
		super.postEdit(e);
	}
	
}

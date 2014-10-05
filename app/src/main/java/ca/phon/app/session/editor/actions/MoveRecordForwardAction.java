package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class MoveRecordForwardAction extends SessionEditorAction {

	private static final long serialVersionUID = 3234859105068487762L;
	
	private final String TXT = "Move record forward";
	
	private final String DESC = "Move record foward in session";

	public MoveRecordForwardAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		final int position = editor.getCurrentRecordIndex() + 1;
		
		if(position >= 0 && position < session.getRecordCount()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}

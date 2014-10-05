package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class MoveRecordToEndAction extends SessionEditorAction {

	private static final long serialVersionUID = 807539158703693401L;
	
	private final String TXT = "Move record to end";
	
	private final String DESC = "Move current record to end of session";

	public MoveRecordToEndAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		final int position = session.getRecordCount()-1;
		
		if(position >= 0) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}

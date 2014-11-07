package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;

public class MoveRecordToBeginningAction extends SessionEditorAction {

	private static final long serialVersionUID = 1L;
	
	private final String TXT = "Move record to beginning";
	
	private final String DESC = "Move current record to beginning of session";

	public MoveRecordToBeginningAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		final int position = 0;
		
		if(position != editor.getCurrentRecordIndex()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
}

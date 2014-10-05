package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class MoveRecordBackwardAction extends SessionEditorAction {

	private static final long serialVersionUID = 7866859643630125691L;

	private final String TXT = "Move record backward";
	
	private final String DESC = "Move record backward in session";

	public MoveRecordBackwardAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		final int position = editor.getCurrentRecordIndex() - 1;
		
		if(position >= 0 && position < session.getRecordCount()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
}

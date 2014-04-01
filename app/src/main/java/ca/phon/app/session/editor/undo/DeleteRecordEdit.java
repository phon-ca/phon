package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

/**
 * Delete the current record.
 */
public class DeleteRecordEdit extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = -4840525709931298024L;

	private int recordIndex = -1;
	
	private Record deletedRecord = null;

	public DeleteRecordEdit(SessionEditor editor) {
		super(editor);
	}
	
	@Override
	public void undo() {
		if(deletedRecord == null || recordIndex < 0) return;
		
		final SessionEditor editor = getEditor();
		editor.getSession().addRecord(recordIndex, deletedRecord);
		
		queueEvent(EditorEventType.RECORD_ADDED_EVT, editor.getUndoSupport(), deletedRecord);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		
		if(editor.getDataModel().getRecordCount() > 1) {
			recordIndex = editor.getCurrentRecordIndex();
			deletedRecord = editor.currentRecord();
			editor.getSession().removeRecord(deletedRecord);
		}
		
		queueEvent(EditorEventType.RECORD_DELETED_EVT, getSource(), deletedRecord);
	}

}

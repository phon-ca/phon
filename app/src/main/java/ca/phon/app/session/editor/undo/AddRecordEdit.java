package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;
import ca.phon.session.Session;

/**
 * Edit performed when a new record is added.
 * 
 */
public class AddRecordEdit extends SessionEditorUndoableEdit {

	// the added record
	private final Record record;
	
	// the insertion point
	private final int index;
	
	public AddRecordEdit(SessionEditor editor, Record record) {
		this(editor, record, -1);
	}
	
	public AddRecordEdit(SessionEditor editor, Record record, int index) {
		super(editor);
		this.record = record;
		this.index = index;
	}
	
	@Override
	public void undo() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.removeRecord(record);
		queueEvent(EditorEventType.RECORD_DELETED_EVT, editor.getUndoSupport(), record);
	}
	
	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(index < 0)
			session.addRecord(record);
		else
			session.addRecord(index, record);
		queueEvent(EditorEventType.RECORD_ADDED_EVT, getSource(), editor);
	}

}

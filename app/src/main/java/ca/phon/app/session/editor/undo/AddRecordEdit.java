package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;

/**
 * Edit performed when a new record is added.
 * 
 */
public class AddRecordEdit extends SessionEditorUndoableEdit {

	// the added record
	private Record record;
	
	// the insertion point
	private final int index;
	
	public AddRecordEdit(SessionEditor editor) {
		this(editor, null, -1);
	}
	
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
		
		if(record == null) {
			final SessionFactory factory = SessionFactory.newFactory();
			record = factory.createRecord();
			record.addGroup();
			final Tier<MediaSegment> segTier = record.getSegment();
			segTier.setGroup(0, factory.createMediaSegment());
		}
		
		if(index < 0)
			session.addRecord(record);
		else
			session.addRecord(index, record);
		queueEvent(EditorEventType.RECORD_ADDED_EVT, getSource(), record);
	}

}

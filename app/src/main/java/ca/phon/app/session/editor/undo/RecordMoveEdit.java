package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class RecordMoveEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -1153685660041411904L;

	private final Record record;
	
	private final int position;
	
	private int oldPosition = -1;
	
	private boolean issueRefresh = true;
	
	public RecordMoveEdit(SessionEditor editor, Record record, int position) {
		super(editor);
		this.record = record;
		this.position = position;
	}
	
	public boolean isIssueRefresh() {
		return this.issueRefresh;
	}
	
	public void setIssueRefresh(boolean issueRefresh) {
		this.issueRefresh = issueRefresh;
	}
	
	@Override
	public boolean canUndo() {
		return oldPosition >= 0;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setRecordPosition(record, oldPosition);
		
		queueEvent(EditorEventType.RECORD_MOVED_EVT, getSource(), record);
		if(issueRefresh)
			queueEvent(EditorEventType.RECORD_REFRESH_EVT, getSource(), null);
	}

	@Override
	public void doIt() {
		oldPosition = getEditor().getSession().getRecordPosition(record);
		getEditor().getSession().setRecordPosition(record, position);
		
		queueEvent(EditorEventType.RECORD_MOVED_EVT, getSource(), record);
		if(issueRefresh)
			queueEvent(EditorEventType.RECORD_REFRESH_EVT, getSource(), null);
	}

}

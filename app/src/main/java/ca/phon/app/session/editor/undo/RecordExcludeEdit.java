package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

public class RecordExcludeEdit extends SessionEditorUndoableEdit {

	private final Record record;
	
	private final boolean exclude;
	
	private final boolean wasExcluded;
	
	public RecordExcludeEdit(SessionEditor editor, Record record, boolean exclude) {
		super(editor);
		this.record = record;
		this.exclude = exclude;
		this.wasExcluded = record.isExcludeFromSearches();
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change record exclusion from searches";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change record exclusion from searches";
	}

	@Override
	public void undo() throws CannotUndoException {
		record.setExcludeFromSearches(wasExcluded);
		
		queueEvent(EditorEventType.RECORD_EXCLUDE_CHANGE_EVT, getEditor().getUndoSupport(), record);
	}

	@Override
	public void doIt() {
		record.setExcludeFromSearches(this.exclude);
		
		queueEvent(EditorEventType.RECORD_EXCLUDE_CHANGE_EVT, getSource(), record);
	}

}

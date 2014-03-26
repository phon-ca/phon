package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

/**
 * Undo-able edit for splitting groups.
 */
public class SplitGroupEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -7260735541114380839L;

	private final WeakReference<Record> recordRef;
	
	private final int gIndex;
	
	private final int wIndex;
	
	public SplitGroupEdit(SessionEditor editor, Record record, int gIndex, int eleIndex) {
		super(editor);
		this.recordRef = new WeakReference<Record>(record);
		this.gIndex = gIndex;
		this.wIndex = eleIndex;
	}
	
	public Record getRecord() {
		return recordRef.get();
	}
	
	@Override
	public void doIt() {
		final Record record = getRecord();
		if(record == null) return;
		
		record.splitGroup(gIndex, wIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}

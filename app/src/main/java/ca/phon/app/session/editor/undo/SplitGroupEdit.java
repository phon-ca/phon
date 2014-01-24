package ca.phon.app.session.editor.undo;

import java.lang.ref.WeakReference;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Word;

public class SplitGroupEdit extends SessionEditorUndoableEdit {

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
		
		final Group group = (gIndex < record.numberOfGroups() ? record.getGroup(gIndex) : null);
		if(group == null) return;
		
		// split group data
		record.addGroup(gIndex+1);
		final Group newGroup = record.getGroup(gIndex+1);
		
		for(int widx = wIndex; widx < group.getAlignedWordCount(); widx++) {
			final Word word = group.getAlignedWord(widx);
		}
	}

}

package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Group;
import ca.phon.session.Record;

public class RemoveGroupEdit extends SessionEditorUndoableEdit {

	/**
	 * tier
	 */
	private final Record record;
	
	/** 
	 * group index
	 */
	private final int groupIndex;
	
	private Group oldGroup;
	
	public RemoveGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}
	
	@Override
	public void doIt() {
		oldGroup = record.getGroup(groupIndex);
		
		record.removeGroup(groupIndex);	
	}

}

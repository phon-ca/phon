package ca.phon.app.session.editor.undo;

import java.util.HashMap;
import java.util.Map;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.SystemTierType;

/**
 * Edit which will merge a group with the next adjacent group.
 */
public class MergeGroupEdit extends SessionEditorUndoableEdit {
	
	private final Record record;

	private final int groupIndex;
	
	private int wordIndex;
	
	public MergeGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		record.splitGroup(groupIndex, wordIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}
	
	@Override
	public void doIt() {
		if(groupIndex+1 >= record.numberOfGroups()) return;
		
		wordIndex = record.mergeGroups(groupIndex, groupIndex+1);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}

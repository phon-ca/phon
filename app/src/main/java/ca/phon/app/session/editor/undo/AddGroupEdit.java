package ca.phon.app.session.editor.undo;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Tier;

public class AddGroupEdit extends SessionEditorUndoableEdit {
	
	/**
	 * tier
	 */
	private final Record record;
	
	/** 
	 * group index
	 */
	private final int groupIndex;
	
	public AddGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}
	
	

	@Override
	public String getRedoPresentationName() {
		return "Redo add group";
	}



	@Override
	public String getUndoPresentationName() {
		return "Undo add group";
	}

	@Override
	public void undo() throws CannotUndoException {
		record.removeGroup(groupIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getEditor().getUndoSupport(), null);
	}

	@Override
	public void doIt() {
		record.addGroup(groupIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), record.getGroup(groupIndex));
	}

}

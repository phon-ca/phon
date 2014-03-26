package ca.phon.app.session.editor.view.record_data.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;

public abstract class RecordDataEditorViewAction extends AbstractAction {

	private static final long serialVersionUID = 7623979692728491952L;

	private final RecordDataEditorView editor;
	
	private Record record;
	
	private int index = -1;
	
	public RecordDataEditorViewAction(RecordDataEditorView editor) {
		super();
		this.editor = editor;
	}
	
	public RecordDataEditorView getEditorView() {
		return this.editor;
	}
	
	public Record getRecord() {
		Record r = record;
		if(r == null)
			r = editor.getEditor().currentRecord();
		return r;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	/**
	 * Returns the group index.  If not set, will return the current
	 * group index as indicated by the editor view or last group
	 * in the record.
	 * 
	 * @return
	 */
	public int getIndex() {
		int idx = (index < 0 ? 
				(editor.currentGroupIndex() < 0 ? getRecord().numberOfGroups() - 1 : editor.currentGroupIndex())
				: index);
		return idx;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}

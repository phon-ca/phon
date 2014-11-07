package ca.phon.app.session.editor.undo;

import java.awt.Component;
import java.lang.ref.WeakReference;

import javax.swing.FocusManager;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
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
	public void undo() {
		final Record record = getRecord();
		if(record == null) return;
		
		record.mergeGroups(gIndex, gIndex+1);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getEditor().getUndoSupport(), null);
	}
	
	@Override
	public void doIt() {
		RecordDataEditorView recordDataView = 
				(RecordDataEditorView)getEditor().getViewModel().getView(RecordDataEditorView.VIEW_NAME);
		if(recordDataView.currentGroupIndex() == gIndex) {
			final Component focusedComp = 
					FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusedComp != null && focusedComp instanceof GroupField) {
				final GroupField<?> grpField = (GroupField<?>)focusedComp;
				grpField.validateAndUpdate();
			}
		}
		
		final Record record = getRecord();
		if(record == null) return;
		
		int wIdx = wIndex;
		if(wIdx < 0) {
			record.addGroup(gIndex);
		} else if(wIdx >= record.getGroup(gIndex).getAlignedWordCount()) {
			record.addGroup(gIndex+1);
		} else {
			record.splitGroup(gIndex, wIdx);
		}
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}

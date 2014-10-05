package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.RecordSortDialog;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;

public class SortRecordsAction extends SessionEditorAction {

	private static final long serialVersionUID = 4380918122333098115L;
	
	private final static String TXT = "Sort records...";
	
	public SortRecordsAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// show 'Sort records...' dialog
		final RecordSortDialog dialog = new RecordSortDialog(getEditor().getSession());
		dialog.pack();
		dialog.setModal(true);
		dialog.setLocationRelativeTo(getEditor());
		dialog.setVisible(true);
		
		if(dialog.wasCanceled()) return;
		
		final List<Record> sortedRecords = new ArrayList<Record>();
		for(Record record:getEditor().getSession().getRecords()) {
			sortedRecords.add(record);
		}
		Collections.sort(sortedRecords, dialog.getComparator());
		
		final CompoundEdit cmpEdit = new CompoundEdit() {

			@Override
			public String getUndoPresentationName() {
				return "Undo sort records";
			}

			@Override
			public String getRedoPresentationName() {
				return "Redo sort records";
			}
			
		};
		for(int i = 0; i < sortedRecords.size(); i++) {
			final Record r = sortedRecords.get(i);
			
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), r, i);
			edit.setIssueRefresh(i == (sortedRecords.size()-1));
			edit.doIt();
			
			cmpEdit.addEdit(edit);
		}
		cmpEdit.end();
		
		getEditor().getUndoSupport().postEdit(cmpEdit);
	}

}

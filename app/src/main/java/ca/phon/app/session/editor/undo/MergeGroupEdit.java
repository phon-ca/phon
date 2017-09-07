/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor.undo;

import java.awt.Component;

import javax.swing.FocusManager;
import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;

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
		
		int wIdx = wordIndex;
		if(wIdx < 0) {
			record.addGroup(groupIndex);
		} else if(wIdx >= record.getGroup(groupIndex).getAlignedWordCount()) {
			record.addGroup(groupIndex+1);
		} else {
			record.splitGroup(groupIndex, wIdx);
		}
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}
	
	@Override
	public void doIt() {
		RecordDataEditorView recordDataView = 
				(RecordDataEditorView)getEditor().getViewModel().getView(RecordDataEditorView.VIEW_NAME);
		if(recordDataView.currentGroupIndex() == groupIndex) {
			final Component focusedComp = 
					FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusedComp != null && focusedComp instanceof GroupField) {
				final GroupField<?> grpField = (GroupField<?>)focusedComp;
				grpField.validateAndUpdate();
			}
		}
		
		if(groupIndex+1 >= record.numberOfGroups()) return;
		
		wordIndex = record.mergeGroups(groupIndex, groupIndex+1);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), null);
	}

}

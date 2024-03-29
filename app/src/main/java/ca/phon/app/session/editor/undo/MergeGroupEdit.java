/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Record;

import javax.swing.FocusManager;
import javax.swing.undo.CannotUndoException;
import java.awt.*;

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

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, (Component) getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		RecordDataEditorView recordDataView = 
				(RecordDataEditorView)getEditor().getViewModel().getView(RecordDataEditorView.VIEW_NAME);
		if(recordDataView.currentGroupIndex() == groupIndex) {
			final Component focusedComp = 
					FocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if(focusedComp instanceof GroupField) {
				final GroupField<?> grpField = (GroupField<?>)focusedComp;
				grpField.validateAndUpdate();
			}
		}
		
		if(groupIndex+1 >= record.numberOfGroups()) return;
		
		wordIndex = record.mergeGroups(groupIndex, groupIndex+1);

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, (Component) getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
	}

}

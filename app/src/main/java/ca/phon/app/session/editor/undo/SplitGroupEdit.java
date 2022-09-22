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
import java.awt.*;
import java.lang.ref.WeakReference;

/**
 * Undo-able edit for splitting groups.
 */
public class SplitGroupEdit extends SessionEditorUndoableEdit {

	private final WeakReference<Record> recordRef;
	
	private final int gIndex;
	
	private final int wIndex;
	
	public SplitGroupEdit(SessionEditor editor, Record record, int gIndex, int eleIndex) {
		super(editor);
		this.recordRef = new WeakReference<>(record);
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

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
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

		final EditorEvent<Void> ee = new EditorEvent<>(EditorEventType.GroupListChange, getSource(), null);
		getEditor().getEventManager().queueEvent(ee);
	}

}

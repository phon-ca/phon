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
import ca.phon.session.Record;

import java.awt.*;

/**
 * Delete the current record.
 */
public class DeleteRecordEdit extends SessionEditorUndoableEdit {

	private int recordIndex = -1;

	private int elementIndex = -1;

	private Record deletedRecord = null;

	private boolean fireEvent = true;

	public DeleteRecordEdit(SessionEditor editor) {
		super(editor);
	}
	
	public DeleteRecordEdit(SessionEditor editor, int recordIndex) {
		super(editor);
		
		this.recordIndex = recordIndex;
	}

	public void setFireEvent(boolean fireEvent) {
		this.fireEvent = fireEvent;
	}

	@Override
	public void undo() {
		super.undo();

		if(deletedRecord == null || recordIndex < 0) return;

		final SessionEditor editor = getEditor();
		editor.getSession().addRecord(recordIndex, deletedRecord);

		if(fireEvent) {
			final EditorEvent<EditorEventType.RecordAddedData> ee =
					new EditorEvent<>(EditorEventType.RecordAdded, getSource(), new EditorEventType.RecordAddedData(deletedRecord, elementIndex, recordIndex));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();

		if(recordIndex < 0) {
			recordIndex = editor.getCurrentRecordIndex();
		}
		elementIndex = editor.getSession().getRecordElementIndex(recordIndex);
		deletedRecord = editor.getSession().getRecord(recordIndex);
		editor.getSession().removeRecord(deletedRecord);

		if(fireEvent) {
			final EditorEvent<EditorEventType.RecordDeletedData> ee =
					new EditorEvent<>(EditorEventType.RecordDeleted, getSource(), new EditorEventType.RecordDeletedData(deletedRecord, elementIndex, recordIndex));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}

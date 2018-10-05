/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

/**
 * Delete the current record.
 */
public class DeleteRecordEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -4840525709931298024L;

	private int recordIndex = -1;

	private Record deletedRecord = null;

	public DeleteRecordEdit(SessionEditor editor) {
		super(editor);
	}

	@Override
	public void undo() {
		super.undo();

		if(deletedRecord == null || recordIndex < 0) return;

		final SessionEditor editor = getEditor();
		editor.getSession().addRecord(recordIndex, deletedRecord);

		queueEvent(EditorEventType.RECORD_ADDED_EVT, editor.getUndoSupport(), deletedRecord);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();

//		if(editor.getDataModel().getRecordCount() > 1) {
			recordIndex = editor.getCurrentRecordIndex();
			deletedRecord = editor.currentRecord();
			editor.getSession().removeRecord(deletedRecord);
//		}

		queueEvent(EditorEventType.RECORD_DELETED_EVT, getSource(), deletedRecord);
	}

}

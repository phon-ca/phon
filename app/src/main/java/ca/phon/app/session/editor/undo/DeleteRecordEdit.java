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
import ca.phon.session.Session;
import ca.phon.session.Transcript;
import ca.phon.ui.CommonModuleFrame;

/**
 * Delete the current record.
 */
public class DeleteRecordEdit extends SessionUndoableEdit {

	private int recordIndex = -1;

	private int elementIndex = -1;

	private Record deletedRecord = null;

	private boolean fireEvent = true;

	public DeleteRecordEdit(SessionEditor editor, int recordIndex) {
		this(editor.getSession(), editor.getEventManager(), recordIndex);
	}

	public DeleteRecordEdit(Session session, EditorEventManager editorEventManager, int recordIndex) {
		super(session, editorEventManager);
		
		this.recordIndex = recordIndex;
	}

	public void setFireEvent(boolean fireEvent) {
		this.fireEvent = fireEvent;
	}

	@Override
	public void undo() {
		super.undo();

		if(deletedRecord == null || recordIndex < 0) return;
		getSession().addRecord(recordIndex, deletedRecord);

		if(fireEvent) {
			final EditorEvent<EditorEventType.ElementAddedData> elementAddedEvt =
					new EditorEvent<>(EditorEventType.ElementAdded, getSource(),
							new EditorEventType.ElementAddedData(getSession().getTranscript().getElementAt(elementIndex), elementIndex));
			getEditorEventManager().queueEvent(elementAddedEvt);
			final EditorEvent<EditorEventType.RecordAddedData> ee =
					new EditorEvent<>(EditorEventType.RecordAdded, getSource(), new EditorEventType.RecordAddedData(deletedRecord, elementIndex, recordIndex));
			getEditorEventManager().queueEvent(ee);
		}
	}

	@Override
	public void doIt() {
		if(recordIndex < 0 || recordIndex >= getSession().getRecordCount()) return;

		elementIndex = getSession().getRecordElementIndex(recordIndex);
		final Transcript.Element removedElement = getSession().getTranscript().getElementAt(elementIndex);
		deletedRecord = getSession().getRecord(recordIndex);
		getSession().removeRecord(deletedRecord);

		if(fireEvent) {
			final EditorEvent<EditorEventType.ElementDeletedData> elementDeletedEvt =
					new EditorEvent<>(EditorEventType.ElementDeleted, getSource(),
							new EditorEventType.ElementDeletedData(removedElement, elementIndex));
			getEditorEventManager().queueEvent(elementDeletedEvt);
			final EditorEvent<EditorEventType.RecordDeletedData> ee =
					new EditorEvent<>(EditorEventType.RecordDeleted, getSource(), new EditorEventType.RecordDeletedData(deletedRecord, elementIndex, recordIndex));
			getEditorEventManager().queueEvent(ee);
		}
	}

}

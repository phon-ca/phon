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
import ca.phon.session.*;

/**
 * Edit performed when a new record is added.
 * 
 */
public class AddRecordEdit extends SessionUndoableEdit {

	// the added record
	private Record record;

	// the insertion point
	private int elementIndex;

	// record index
	private int recordIndex;
	
	private boolean fireEvent = true;

	public AddRecordEdit(SessionEditor editor) {
		this(editor.getSession(), editor.getEventManager());
	}

	public AddRecordEdit(Session session, EditorEventManager editorEventManager) {
		this(session, editorEventManager, null, -1);
	}

	public AddRecordEdit(SessionEditor editor, Record record) {
		this(editor.getSession(), editor.getEventManager(), record);
	}

	public AddRecordEdit(Session session, EditorEventManager editorEventManager, Record record) {
		this(session, editorEventManager, record, -1);
	}

	public AddRecordEdit(SessionEditor editor, Record record, int elementIndex) {
		this(editor.getSession(), editor.getEventManager(), record, elementIndex);
	}
	
	public AddRecordEdit(Session session, EditorEventManager editorEventManager, Record record, int elementIndex) {
		super(session, editorEventManager);
		this.record = record;
		this.elementIndex = elementIndex;
	}

	public void setFireEvent(boolean fireEvent) {
		this.fireEvent = fireEvent;
	}
	
	public boolean isFireEvent() {
		return this.fireEvent;
	}
	
	@Override
	public String getUndoPresentationName() {
		return "Undo add record " + record.getUuid().toString();
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo add record " + record.getUuid().toString();
	}

	public Record getRecord() {
		return this.record;
	}

	@Override
	public void undo() {
		final Transcript.Element removedElement = getSession().getTranscript().getElementAt(elementIndex);

		super.undo();
		
		final EditorEventManager editorEventManager = getEditorEventManager();
		final Session session = getSession();
		
		session.removeRecord(record);
		if(isFireEvent()) {
			final EditorEvent<EditorEventType.ElementDeletedData> elementDeletedEvt =
					new EditorEvent<>(EditorEventType.ElementDeleted, getSource(),
							new EditorEventType.ElementDeletedData(removedElement, elementIndex));
			getEditorEventManager().queueEvent(elementDeletedEvt);
			final EditorEvent<EditorEventType.RecordDeletedData> ee =
					new EditorEvent<>(EditorEventType.RecordDeleted, getSource(), new EditorEventType.RecordDeletedData(record, elementIndex, recordIndex));
			editorEventManager.queueEvent(ee);
		}
	}
	
	@Override
	public void doIt() {
		final EditorEventManager editorEventManager = getEditorEventManager();
		final Session session = getSession();
		
		if(record == null) {
			final SessionFactory factory = SessionFactory.newFactory();
			record = factory.createRecord(session);
			record.setSpeaker(Participant.UNKNOWN);
		}

		if(elementIndex < 0) elementIndex = session.getTranscript().getNumberOfElements();
		session.getTranscript().addElement(elementIndex, new Transcript.Element(record));
		recordIndex = session.getTranscript().getRecordIndex(elementIndex);

		if(isFireEvent()) {
			final EditorEvent<EditorEventType.ElementAddedData> elementAddedEvt =
					new EditorEvent<>(EditorEventType.ElementAdded, getSource(),
							new EditorEventType.ElementAddedData(getSession().getTranscript().getElementAt(elementIndex), elementIndex));
			getEditorEventManager().queueEvent(elementAddedEvt);
			final EditorEvent<EditorEventType.RecordAddedData> ee =
					new EditorEvent<>(EditorEventType.RecordAdded, getSource(), new EditorEventType.RecordAddedData(record, elementIndex, recordIndex));
			editorEventManager.queueEvent(ee);
		}
	}

}

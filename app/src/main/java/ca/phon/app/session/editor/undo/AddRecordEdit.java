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
public class AddRecordEdit extends SessionEditorUndoableEdit {

	// the added record
	private Record record;
	
	// the insertion point
	private int index;
	
	private boolean fireEvent = true;

	public AddRecordEdit(SessionEditor editor) {
		this(editor, null, -1);
	}
	
	public AddRecordEdit(SessionEditor editor, Record record) {
		this(editor, record, -1);
	}
	
	public AddRecordEdit(SessionEditor editor, Record record, int index) {
		super(editor);
		this.record = record;
		this.index = index;
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
		super.undo();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.removeRecord(record);
		if(isFireEvent()) {
			final EditorEvent<EditorEventType.RecordDeletedData> ee =
					new EditorEvent<>(EditorEventType.RecordDeleted, getSource(), new EditorEventType.RecordDeletedData(index, record));
			getEditor().getEventManager().queueEvent(ee);
		}
	}
	
	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(record == null) {
			final SessionFactory factory = SessionFactory.newFactory();
			record = factory.createRecord();
			record.setSpeaker(Participant.UNKNOWN);
			record.addGroup();
		}
		
		if(index < 0) {
			session.addRecord(record);
			index = session.getRecordCount() - 1;
		} else
			session.addRecord(index, record);
		
		if(isFireEvent()) {
			final EditorEvent<EditorEventType.RecordAddedData> ee =
					new EditorEvent<>(EditorEventType.RecordAdded, getSource(), new EditorEventType.RecordAddedData(index, record));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}

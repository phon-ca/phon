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
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;

/**
 * Edit performed when a new record is added.
 * 
 */
public class AddRecordEdit extends SessionEditorUndoableEdit {

	// the added record
	private Record record;
	
	// the insertion point
	private final int index;
	
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
	
	@Override
	public void undo() {
		super.undo();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.removeRecord(record);
		if(isFireEvent())
			queueEvent(EditorEventType.RECORD_DELETED_EVT, editor.getUndoSupport(), record);
	}
	
	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(record == null) {
			final SessionFactory factory = SessionFactory.newFactory();
			record = factory.createRecord();
			record.addGroup();
			final Tier<MediaSegment> segTier = record.getSegment();
			segTier.setGroup(0, factory.createMediaSegment());
		}
		
		if(index < 0)
			session.addRecord(record);
		else
			session.addRecord(index, record);
		
		if(isFireEvent())
			queueEvent(EditorEventType.RECORD_ADDED_EVT, getSource(), record);
	}

}

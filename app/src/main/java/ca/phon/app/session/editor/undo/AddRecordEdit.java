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

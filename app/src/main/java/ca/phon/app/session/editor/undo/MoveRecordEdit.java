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

import javax.swing.undo.CannotUndoException;

public class MoveRecordEdit extends SessionUndoableEdit {

	private final Record record;
	
	private final int position;
	
	private int oldPosition = -1;

	private int oldElementIndex = -1;

	private int newElementIndex = -1;
	
	private boolean issueRefresh = true;

	public MoveRecordEdit(SessionEditor editor, Record record, int position) {
		this(editor.getSession(), editor.getEventManager(), record, position);
	}

	public MoveRecordEdit(Session session, EditorEventManager editorEventManager, Record record, int position) {
		super(session, editorEventManager);
		this.record = record;
		this.position = position;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo move record " + record.getUuid().toString();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo move record " + record.getUuid().toString();
	}
	
	public boolean isIssueRefresh() {
		return this.issueRefresh;
	}
	
	public void setIssueRefresh(boolean issueRefresh) {
		this.issueRefresh = issueRefresh;
	}
	
	@Override
	public boolean canUndo() {
		return oldPosition >= 0;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final Session session = getSession();
		
		session.setRecordPosition(record, oldPosition);

		final EditorEvent<EditorEventType.ElementMovedData> elementMovedEvt =
				new EditorEvent<>(EditorEventType.ElementMoved, getSource(),
						new EditorEventType.ElementMovedData(getSession().getTranscript().getElementAt(oldElementIndex), newElementIndex, oldElementIndex));
		getEditorEventManager().queueEvent(elementMovedEvt);

		final EditorEvent<EditorEventType.RecordMovedData> ee =
				new EditorEvent<>(EditorEventType.RecordMoved, getSource(),
						new EditorEventType.RecordMovedData(record, newElementIndex, position, oldElementIndex, oldPosition));
		getEditorEventManager().queueEvent(ee);
		if(issueRefresh) {
			getEditorEventManager().queueEvent(
					new EditorEvent<>(EditorEventType.RecordRefresh, getSource(),
							new EditorEventType.RecordChangedData(record, oldElementIndex, oldPosition)));
//			getEditorEventManager().setCurrentRecordIndex(oldPosition);
		}
	}

	@Override
	public void doIt() {
		oldPosition = getSession().getRecordPosition(record);
		oldElementIndex = getSession().getRecordElementIndex(record);
		getSession().setRecordPosition(record, position);
		newElementIndex = getSession().getRecordElementIndex(record);

		final EditorEvent<EditorEventType.ElementMovedData> elementMovedEvt =
				new EditorEvent<>(EditorEventType.ElementMoved, getSource(),
						new EditorEventType.ElementMovedData(getSession().getTranscript().getElementAt(newElementIndex), oldElementIndex, newElementIndex));
		getEditorEventManager().queueEvent(elementMovedEvt);

		final EditorEvent<EditorEventType.RecordMovedData> ee =
				new EditorEvent<>(EditorEventType.RecordMoved, getSource(),
						new EditorEventType.RecordMovedData(record, oldElementIndex, oldPosition, newElementIndex, position));
		getEditorEventManager().queueEvent(ee);
		if(issueRefresh) {
			getEditorEventManager().queueEvent(
					new EditorEvent<>(EditorEventType.RecordRefresh, getSource(),
							new EditorEventType.RecordChangedData(record, newElementIndex, position)));
//			getEditorEventManager().setCurrentRecordIndex(position);
		}
	}

}

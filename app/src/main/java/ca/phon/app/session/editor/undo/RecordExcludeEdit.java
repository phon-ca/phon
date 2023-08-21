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

import javax.swing.undo.CannotUndoException;

public class RecordExcludeEdit extends SessionUndoableEdit {

	private final Record record;
	
	private final boolean exclude;
	
	private final boolean wasExcluded;
	
	public RecordExcludeEdit(SessionEditor editor, Record record, boolean exclude) {
		this(editor.getSession(), editor.getEventManager(), record, exclude);
	}

	public RecordExcludeEdit(Session session, EditorEventManager editorEventManager, Record record, boolean exclude) {
		super(session, editorEventManager);
		this.record = record;
		this.exclude = exclude;
		this.wasExcluded = record.isExcludeFromSearches();
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change record exclusion from searches";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change record exclusion from searches";
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		record.setExcludeFromSearches(wasExcluded);

		final EditorEvent<EditorEventType.RecordExcludedChangedData> ee =
				new EditorEvent<>(EditorEventType.RecordExcludedChanged, getSource(), new EditorEventType.RecordExcludedChangedData(record, wasExcluded));
		getEditorEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		record.setExcludeFromSearches(this.exclude);

		final EditorEvent<EditorEventType.RecordExcludedChangedData> ee =
				new EditorEvent<>(EditorEventType.RecordExcludedChanged, getSource(), new EditorEventType.RecordExcludedChangedData(record, exclude));
		getEditorEventManager().queueEvent(ee);
	}

}

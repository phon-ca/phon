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

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Record;

public class ChangeSpeakerEdit extends SessionEditorUndoableEdit {

	private final Record record;
	
	private final Participant speaker;
	
	private final Participant oldSpeaker;
	
	public ChangeSpeakerEdit(SessionEditor editor, Record record, Participant speaker) {
		super(editor);
		this.record = record;
		oldSpeaker = record.getSpeaker();
		this.speaker = speaker;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo change record speaker";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change record speaker";
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		record.setSpeaker(oldSpeaker);
		
		queueEvent(EditorEventType.RECORD_SPEAKER_CHANGED_EVT, getEditor().getUndoSupport(), record);
	}

	@Override
	public void doIt() {
		record.setSpeaker(speaker);
		
		queueEvent(EditorEventType.RECORD_SPEAKER_CHANGED_EVT, getSource(), record);
	}

}

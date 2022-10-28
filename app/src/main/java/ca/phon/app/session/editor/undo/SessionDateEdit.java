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
import ca.phon.session.Session;

import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.time.LocalDate;

public class SessionDateEdit extends SessionEditorUndoableEdit {
	
	private final LocalDate newDate;
	
	private final LocalDate prevDate;
	
	public SessionDateEdit(SessionEditor editor, LocalDate newDate, LocalDate prevDate) {
		super(editor);
		this.newDate = newDate;
		this.prevDate = prevDate;
	}
	
	public LocalDate getNewDate() {
		return this.newDate;
	}
	
	public LocalDate getPrevDate() {
		return this.prevDate;
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo set session date";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo set session date";
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setDate(getPrevDate());

		final EditorEvent<EditorEventType.SessionDateChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionDateChanged, editor, new EditorEventType.SessionDateChangedData(getNewDate(), getPrevDate()));
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setDate(getNewDate());

		final EditorEvent<EditorEventType.SessionDateChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionDateChanged, getSource(), new EditorEventType.SessionDateChangedData(getPrevDate(), getNewDate()));
		getEditor().getEventManager().queueEvent(ee);
	}

}

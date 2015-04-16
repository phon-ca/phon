/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import javax.swing.undo.CannotUndoException;

import org.joda.time.DateTime;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

public class SessionDateEdit extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = 126365404901461662L;

	private final DateTime newDate;
	
	private final DateTime prevDate;
	
	public SessionDateEdit(SessionEditor editor, DateTime newDate, DateTime prevDate) {
		super(editor);
		this.newDate = newDate;
		this.prevDate = prevDate;
	}
	
	public DateTime getNewDate() {
		return this.newDate;
	}
	
	public DateTime getPrevDate() {
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
		
		queueEvent(EditorEventType.SESSION_DATE_CHANGED, getEditor().getUndoSupport(), getPrevDate());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setDate(getNewDate());
		
		queueEvent(EditorEventType.SESSION_DATE_CHANGED, getSource(), getNewDate());
	}

}

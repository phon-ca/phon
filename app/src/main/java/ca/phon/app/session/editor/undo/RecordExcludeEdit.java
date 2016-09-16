/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;

public class RecordExcludeEdit extends SessionEditorUndoableEdit {

	private final Record record;
	
	private final boolean exclude;
	
	private final boolean wasExcluded;
	
	public RecordExcludeEdit(SessionEditor editor, Record record, boolean exclude) {
		super(editor);
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
		
		queueEvent(EditorEventType.RECORD_EXCLUDE_CHANGE_EVT, getEditor().getUndoSupport(), record);
	}

	@Override
	public void doIt() {
		record.setExcludeFromSearches(this.exclude);
		
		queueEvent(EditorEventType.RECORD_EXCLUDE_CHANGE_EVT, getSource(), record);
	}

}

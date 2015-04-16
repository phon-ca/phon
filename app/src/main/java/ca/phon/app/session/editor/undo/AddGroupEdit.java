/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

public class AddGroupEdit extends SessionEditorUndoableEdit {
	
	/**
	 * tier
	 */
	private final Record record;
	
	/** 
	 * group index
	 */
	private final int groupIndex;
	
	public AddGroupEdit(SessionEditor editor, Record record, int groupIndex) {
		super(editor);
		this.record = record;
		this.groupIndex = groupIndex;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo add group";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo add group";
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		record.removeGroup(groupIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getEditor().getUndoSupport(), null);
	}

	@Override
	public void doIt() {
		record.addGroup(groupIndex);
		
		queueEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, getSource(), record.getGroup(groupIndex));
	}

}

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

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.*;
import ca.phon.session.*;

public class RecordMoveEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -1153685660041411904L;

	private final Record record;
	
	private final int position;
	
	private int oldPosition = -1;
	
	private boolean issueRefresh = true;
	
	public RecordMoveEdit(SessionEditor editor, Record record, int position) {
		super(editor);
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
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setRecordPosition(record, oldPosition);
		
		queueEvent(EditorEventType.RECORD_MOVED_EVT, getSource(), record);
		if(issueRefresh) {
			queueEvent(EditorEventType.RECORD_REFRESH_EVT, getSource(), null);
			getEditor().setCurrentRecordIndex(oldPosition);
		}
	}

	@Override
	public void doIt() {
		oldPosition = getEditor().getSession().getRecordPosition(record);
		getEditor().getSession().setRecordPosition(record, position);
		
		queueEvent(EditorEventType.RECORD_MOVED_EVT, getSource(), record);
		if(issueRefresh) {
			queueEvent(EditorEventType.RECORD_REFRESH_EVT, getSource(), null);
			getEditor().setCurrentRecordIndex(position);
		}
	}

}

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
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;

public class MoveRecordToBeginningAction extends SessionEditorAction {

	private static final long serialVersionUID = 1L;
	
	private final String TXT = "Move record to beginning";
	
	private final String DESC = "Move current record to beginning of session";

	public MoveRecordToBeginningAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Record record = editor.currentRecord();
		
		final int position = 0;
		
		if(position != editor.getCurrentRecordIndex()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
}

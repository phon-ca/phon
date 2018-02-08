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

import ca.phon.app.session.editor.*;
import ca.phon.session.Record;

/**
 * Delete the current record.
 */
public class DeleteRecordEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -4840525709931298024L;

	private int recordIndex = -1;

	private Record deletedRecord = null;

	public DeleteRecordEdit(SessionEditor editor) {
		super(editor);
	}

	@Override
	public void undo() {
		super.undo();

		if(deletedRecord == null || recordIndex < 0) return;

		final SessionEditor editor = getEditor();
		editor.getSession().addRecord(recordIndex, deletedRecord);

		queueEvent(EditorEventType.RECORD_ADDED_EVT, editor.getUndoSupport(), deletedRecord);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();

//		if(editor.getDataModel().getRecordCount() > 1) {
			recordIndex = editor.getCurrentRecordIndex();
			deletedRecord = editor.currentRecord();
			editor.getSession().removeRecord(deletedRecord);
//		}

		queueEvent(EditorEventType.RECORD_DELETED_EVT, getSource(), deletedRecord);
	}

}

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
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class MoveRecordForwardAction extends SessionEditorAction {

	private static final long serialVersionUID = 3234859105068487762L;
	
	private final String TXT = "Move record forward";
	
	private final String DESC = "Move record foward in session";

	public MoveRecordForwardAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		final int position = editor.getCurrentRecordIndex() + 1;
		
		if(position >= 0 && position < session.getRecordCount()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}

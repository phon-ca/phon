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

import java.lang.ref.WeakReference;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import ca.phon.app.session.editor.SessionEditor;

/**
 * Undo support for the {@link SessionEditor}
 *
 */
public class SessionEditorUndoSupport extends UndoableEditSupport {
	
	private final WeakReference<SessionEditor> editorRef;
	
	public SessionEditorUndoSupport(SessionEditor sessionEditor) {
		editorRef = new WeakReference<SessionEditor>(sessionEditor);
	}

	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	@Override
	public synchronized void postEdit(UndoableEdit e) {
		if(e instanceof SessionEditorUndoableEdit) {
			final SessionEditorUndoableEdit edit = SessionEditorUndoableEdit.class.cast(e);
			
			edit.putExtension(Integer.class, getEditor().getCurrentRecordIndex());
			
			edit.doIt();
		}
		super.postEdit(e);
	}
	
}

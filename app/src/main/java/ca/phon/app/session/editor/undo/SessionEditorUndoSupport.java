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

import java.lang.ref.*;

import javax.swing.undo.*;

import ca.phon.app.session.editor.*;

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

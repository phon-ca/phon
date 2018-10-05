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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;

public class SessionLanguageEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 2753425148054627013L;

	private final String newLang;
	
	private String oldLang;
	
	public SessionLanguageEdit(SessionEditor editor, String newLang) {
		super(editor);
		this.newLang = newLang;
	}

	@Override
	public void undo() {
		getEditor().getSession().setLanguage(oldLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getEditor().getUndoSupport(), oldLang);
		getEditor().getEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		oldLang = getEditor().getSession().getLanguage();
		getEditor().getSession().setLanguage(newLang);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LANG_CHANGED, getSource(), newLang);
		getEditor().getEventManager().queueEvent(ee);
	}

}

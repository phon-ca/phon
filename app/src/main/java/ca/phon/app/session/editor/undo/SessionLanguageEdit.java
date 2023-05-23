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

import ca.phon.app.session.editor.*;

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
		getEditor().getSession().setLanguages(oldLang);

		final EditorEvent<EditorEventType.SessionLangChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionLangChanged, getEditor(), new EditorEventType.SessionLangChangedData(newLang, oldLang));
		getEditor().getEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		oldLang = getEditor().getSession().getLanguages();
		getEditor().getSession().setLanguages(newLang);

		final EditorEvent<EditorEventType.SessionLangChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionLangChanged, getSource(), new EditorEventType.SessionLangChangedData(oldLang, newLang));
		getEditor().getEventManager().queueEvent(ee);
	}

}

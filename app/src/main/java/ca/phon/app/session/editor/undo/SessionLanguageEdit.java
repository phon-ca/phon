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
import ca.phon.session.Session;
import ca.phon.util.Language;

import java.util.List;

public class SessionLanguageEdit extends SessionUndoableEdit {

	private final List<Language> newLangs;
	
	private List<Language> oldLangs;

	public SessionLanguageEdit(SessionEditor editor, List<Language> newLangs) {
		this(editor.getSession(), editor.getEventManager(), newLangs);
	}

	public SessionLanguageEdit(Session session, EditorEventManager editorEventManager, List<Language> newLangs) {
		super(session, editorEventManager);
		this.newLangs = newLangs;
	}

	@Override
	public void undo() {
		getSession().setLanguages(oldLangs);

		final EditorEvent<EditorEventType.SessionLangChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionLangChanged, getSource(), new EditorEventType.SessionLangChangedData(newLangs, oldLangs));
		getEditorEventManager().queueEvent(ee);
	}
	
	@Override
	public void doIt() {
		oldLangs= getSession().getLanguages();
		getSession().setLanguages(newLangs);

		final EditorEvent<EditorEventType.SessionLangChangedData> ee =
				new EditorEvent<>(EditorEventType.SessionLangChanged, getSource(), new EditorEventType.SessionLangChangedData(oldLangs, newLangs));
		getEditorEventManager().queueEvent(ee);
	}

}

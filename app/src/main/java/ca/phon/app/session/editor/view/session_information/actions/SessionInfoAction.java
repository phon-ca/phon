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
package ca.phon.app.session.editor.view.session_information.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;

public abstract class SessionInfoAction extends SessionEditorAction {
	
	private static final long serialVersionUID = 1842274536935284100L;

	private final SessionInfoEditorView view;

	public SessionInfoAction(SessionEditor editor, SessionInfoEditorView view) {
		super(editor);
		this.view = view;
	}
	
	public SessionInfoEditorView getView() {
		return this.view;
	}

}

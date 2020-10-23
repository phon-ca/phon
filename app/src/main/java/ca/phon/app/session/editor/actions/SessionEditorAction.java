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

import java.lang.ref.*;

import javax.swing.*;

import ca.phon.app.hooks.*;
import ca.phon.app.session.editor.*;

/**
 * Base class for {@link SessionEditor} {@link Action}s.
 */
public abstract class SessionEditorAction extends HookableAction {

	private static final long serialVersionUID = -6639660567310323736L;

	private final WeakReference<SessionEditor> editorRef;
	
	public SessionEditorAction(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
	}
	
	public SessionEditor getEditor() {
		return this.editorRef.get();
	}
	
}

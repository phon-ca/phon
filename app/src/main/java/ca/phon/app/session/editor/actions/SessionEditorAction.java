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
package ca.phon.app.session.editor.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Session;

import javax.swing.*;
import java.lang.ref.WeakReference;

/**
 * Base class for {@link SessionEditor} {@link Action}s.
 */
public abstract class SessionEditorAction extends HookableAction {

	private WeakReference<SessionEditor> editorRef;

	private final Session session;

	private final EditorEventManager eventManager;

	private final SessionEditUndoSupport undoSupport;

	/**
	 * Constructor
	 * @param editor
	 * @deprecated
	 */
	@Deprecated
	public SessionEditorAction(SessionEditor editor) {
		this(editor.getSession(), editor.getEventManager(), editor.getUndoSupport());
		this.editorRef = new WeakReference<SessionEditor>(editor);
	}

	/**
	 * Constructor
	 * @param session
	 * @param eventManager
	 * @param undoSupport
	 */
	public SessionEditorAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(null);
		this.session = session;
		this.eventManager = eventManager;
		this.undoSupport = undoSupport;
	}

	/**
	 * Get the session editor
	 * @return the session editor
	 * @deprecated may be null
	 */
	@Deprecated
	public SessionEditor getEditor() {
		return this.editorRef.get();
	}

	public Session getSession() {
		return this.session;
	}

	public EditorEventManager getEventManager() {
		return this.eventManager;
	}

	public SessionEditUndoSupport getUndoSupport() {
		return this.undoSupport;
	}

}

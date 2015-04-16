/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.actions;

import java.lang.ref.WeakReference;

import javax.swing.Action;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.SessionEditor;

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

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

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;

public class MediaLocationEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 7934882593502356426L;

	private final String mediaLocation;
	
	private String oldLocation;
	
	public MediaLocationEdit(SessionEditor editor, String mediaLocation) {
		super(editor);
		this.mediaLocation = mediaLocation;
	}
	
	public String getMediaLocation() {
		return this.mediaLocation;
	}
	
	public String getOldLocation() {
		return this.oldLocation;
	}

	@Override
	public void undo() throws CannotUndoException {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.setMediaLocation(getOldLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, editor.getUndoSupport(), getOldLocation());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		oldLocation = session.getMediaLocation();
		
		String mediaLocation = (getMediaLocation() != null && getMediaLocation().strip().length() > 0 ? getMediaLocation() : null);
		session.setMediaLocation(mediaLocation);
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, getSource(), mediaLocation);
	}

}

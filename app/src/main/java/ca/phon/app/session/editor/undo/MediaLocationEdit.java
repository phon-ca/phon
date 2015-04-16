/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
		
		session.setMediaLocation(getMediaLocation());
		
		queueEvent(EditorEventType.SESSION_MEDIA_CHANGED, getSource(), getMediaLocation());
	}

}

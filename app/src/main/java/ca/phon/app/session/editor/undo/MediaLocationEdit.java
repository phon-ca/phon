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

import javax.swing.undo.CannotUndoException;
import java.awt.*;

public class MediaLocationEdit extends SessionEditorUndoableEdit {

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
		
		if(session.getMediaLocation() == null && getOldLocation() != null
				|| session.getMediaLocation() != null && getOldLocation() == null
				|| session.getMediaLocation() != null && !session.getMediaLocation().equals(getOldLocation())
				|| getOldLocation() != null && !getOldLocation().equals(session.getMediaLocation())) {
			session.setMediaLocation(getOldLocation());
			getEditor().getMediaModel().resetAudioCheck();
			final EditorEvent<EditorEventType.SessionMediaChangedData> ee =
					new EditorEvent<>(EditorEventType.SessionMediaChanged, (Component) getSource(), new EditorEventType.SessionMediaChangedData(getMediaLocation(), getOldLocation()));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(session == null) return;
		
		oldLocation = session.getMediaLocation();
		String mediaLocation = (getMediaLocation() != null && getMediaLocation().strip().length() > 0 ? getMediaLocation() : null);
		
		if(oldLocation == null && mediaLocation != null
				|| oldLocation != null && mediaLocation == null
				|| oldLocation != null && !oldLocation.equals(mediaLocation)) {
			session.setMediaLocation(mediaLocation);
			getEditor().getMediaModel().resetAudioCheck();
			final EditorEvent<EditorEventType.SessionMediaChangedData> ee =
					new EditorEvent<>(EditorEventType.SessionMediaChanged, (Component) getSource(), new EditorEventType.SessionMediaChangedData(getOldLocation(), getMediaLocation()));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}

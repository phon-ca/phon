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
import ca.phon.session.*;

import javax.swing.undo.CannotUndoException;

/**
 * Undo-able edit for adding participants in a {@link Session}.
 *
 */
public class AddParticipantEdit extends SessionUndoableEdit {

	private final Participant participant;

	public AddParticipantEdit(SessionEditor editor, Participant participant) {
		this(editor.getSession(), editor.getEventManager(), participant);
	}

	/**
	 * Constructor
	 */
	public AddParticipantEdit(Session session, EditorEventManager editorEventManager, Participant participant) {
		super(session, editorEventManager);
		this.participant = participant;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}

	@Override
	public boolean canRedo() {
		final EditorEventManager editorEventManager = getEditorEventManager();
		final Session session = getSession();
		
		boolean retVal = true;
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant p = session.getParticipant(i);
			if(getParticipant() == p) {
				retVal = false;
				break;
			}
		}
		return retVal;
	}

	@Override
	public boolean canUndo() {
		final EditorEventManager editor = getEditorEventManager();
		final Session session = getSession();
		
		boolean retVal = false;
		for(int i = 0; i < session.getParticipantCount(); i++) {
			final Participant p = session.getParticipant(i);
			if(getParticipant() == p) {
				retVal = true;
				break;
			}
		}
		return retVal;
	}
	
	@Override
	public String getRedoPresentationName() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Redo add participant '");
		builder.append(getParticipant().getName());
		return builder.toString();
	}

	@Override
	public String getUndoPresentationName() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Undo add participant '");
		builder.append(getParticipant().getName());
		return builder.toString();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		final EditorEventManager editorEventManager = getEditorEventManager();
		final Session session = getSession();
		session.removeParticipant(getParticipant());

		final EditorEvent<Participant> ee = new EditorEvent<>(EditorEventType.ParticipantRemoved, getSource(), getParticipant());
		editorEventManager.queueEvent(ee);
	}

	@Override
	public void doIt() {
		final EditorEventManager editorEventManager = getEditorEventManager();
		final Session session = getSession();
		session.addParticipant(getParticipant());

		final EditorEvent<Participant> ee = new EditorEvent<>(EditorEventType.ParticipantAdded, getSource(), getParticipant());
		editorEventManager.queueEvent(ee);
	}
	
}

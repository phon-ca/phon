/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import ca.phon.app.session.editor.*;
import ca.phon.session.*;

/**
 * Undo-able edit for adding participants in a {@link Session}.
 *
 */
public class AddParticipantEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 2633079803945025310L;
	
	private final Participant participant;
	
	/**
	 * Constructor
	 */
	public AddParticipantEdit(SessionEditor editor, Participant participant) {
		super(editor);
		this.participant = participant;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}

	@Override
	public boolean canRedo() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
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
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
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
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.removeParticipant(getParticipant());
		
		queueEvent(EditorEventType.PARTICIPANT_REMOVED, getEditor().getUndoSupport(), getParticipant());
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.addParticipant(getParticipant());
		
		queueEvent(EditorEventType.PARTICIPANT_ADDED, getSource(), getParticipant());
	}
	
}

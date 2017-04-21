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

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Participant;
import ca.phon.session.Participants;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;

/**
 * {@link Session} edits involving particpants.
 */
public class ParticipantUndoableEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = -5312599132320247077L;

	/**
	 * Participant involved in the edit
	 */
	private Participant participant;
	
	private Participant template;
	
	private Participant oldVals;
	
	public ParticipantUndoableEdit(SessionEditor editor, Participant participant, Participant template) {
		super(editor);
		this.participant = participant;
		this.template = template;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}
	
	@Override
	public void undo() {
		Participants.copyParticipantInfo(oldVals, participant);

		final EditorEvent ee = new EditorEvent(EditorEventType.PARTICIPANT_CHANGED, getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant p = factory.createParticipant();
		Participants.copyParticipantInfo(participant, p);
		oldVals = p;
		
		Participants.copyParticipantInfo(template, participant);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.PARTICIPANT_CHANGED, getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}
	
}

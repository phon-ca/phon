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
package ca.phon.app.session.editor.view.session_information.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.ParticipantUndoableEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.*;
import ca.phon.ui.participant.ParticipantEditor;

public class EditParticipantAction extends SessionInfoAction {

	private static final long serialVersionUID = 552410881946559812L;

	private final Participant participant;
	
	public EditParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, "Edit " + participant.getName() + "...");
		
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant part = factory.createParticipant();
		Participants.copyParticipantInfo(participant, part);
		
		boolean canceled = ParticipantEditor.editParticipant(getEditor(), part, 
				getEditor().getDataModel().getSession().getDate(),
				getEditor().getDataModel().getSession().getParticipants().otherParticipants(participant));
		
		if(!canceled) {
			if(!participant.getId().equals(part.getId())) {
				// XXX we need to ensure that every record is loaded 
				// so that participant information changes when id is modified
				for(Record r:getEditor().getSession().getRecords()) {
					r.getSpeaker();
				}
			}
			final ParticipantUndoableEdit edit = new ParticipantUndoableEdit(getEditor(), participant, part);
			getEditor().getUndoSupport().postEdit(edit);
			
			final EditorEvent ee = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT);
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}

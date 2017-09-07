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

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.*;

public class DeleteParticipantAction extends SessionInfoAction {
	
	private static final long serialVersionUID = -1353836110650283444L;

	private final Participant participant;

	public DeleteParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, "Delete " + participant.getName());
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final CompoundEdit edit = new CompoundEdit();
		final RemoveParticipantEdit removePartEdit = new RemoveParticipantEdit(editor, participant);
		removePartEdit.doIt();
		edit.addEdit(removePartEdit);
		
		for(Record r:session.getRecords()) {
			if(r.getSpeaker() == participant) {
				final ChangeSpeakerEdit chSpeakerEdit = new ChangeSpeakerEdit(editor, r, null);
				chSpeakerEdit.doIt();
				edit.addEdit(chSpeakerEdit);
			}
		}
		edit.end();
		
		editor.getUndoSupport().postEdit(edit);
	}

}

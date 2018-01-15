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

import javax.swing.*;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.icons.*;

public class DeleteParticipantAction extends SessionInfoAction {
	
	private static final long serialVersionUID = -1353836110650283444L;

	private final Participant participant;
	
	private final ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/delete_user", IconSize.SMALL);

	public DeleteParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, "Delete " + participant.getName());
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okCancelOptions);
		props.setHeader("Delete Participant");
		props.setTitle("Delete Participant");
		props.setMessage("Are you sure you wish to delete participant '" + participant + "'?");
		props.setRunAsync(true);
		props.setParentWindow(getEditor());
		props.setListener( (evt) -> {
			if(evt.getDialogResult() == 0) {
				SwingUtilities.invokeLater( () -> deleteParticipant() );
			}
		});
		
		NativeDialogs.showMessageDialog(props);
	}

	private int deleteParticipant() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final CompoundEdit edit = new CompoundEdit();
		final RemoveParticipantEdit removePartEdit = new RemoveParticipantEdit(editor, participant);
		removePartEdit.doIt();
		edit.addEdit(removePartEdit);
		
		int recordsChanged = 0;
		for(Record r:session.getRecords()) {
			if(r.getSpeaker() == participant) {
				final ChangeSpeakerEdit chSpeakerEdit = new ChangeSpeakerEdit(editor, r, null);
				chSpeakerEdit.doIt();
				edit.addEdit(chSpeakerEdit);
				
				++recordsChanged;
			}
		}
		edit.end();
		
		editor.getUndoSupport().postEdit(edit);
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(new String[]{"Ok", "Undo"});
		props.setTitle("Records Modified");
		props.setHeader(String.format("%d Records Modified", recordsChanged));
		props.setMessage("Speaker assigned to 'Unidentified'.");
		props.setRunAsync(true);
		props.setParentWindow(getEditor());
		props.setListener( (e) -> {
			if(e.getDialogResult() == 1) {
				SwingUtilities.invokeLater( () -> {
					getEditor().getUndoManager().undo();
					getEditor().getEventManager().queueEvent(new EditorEvent(EditorEventType.RECORD_REFRESH_EVT));
				});
			}
		});
		NativeDialogs.showMessageDialog(props);
		
		return recordsChanged;
	}
	
}

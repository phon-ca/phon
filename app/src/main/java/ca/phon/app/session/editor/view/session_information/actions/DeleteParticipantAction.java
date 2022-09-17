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
package ca.phon.app.session.editor.view.session_information.actions;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.icons.*;

import javax.swing.*;
import javax.swing.undo.CompoundEdit;
import java.awt.event.ActionEvent;

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

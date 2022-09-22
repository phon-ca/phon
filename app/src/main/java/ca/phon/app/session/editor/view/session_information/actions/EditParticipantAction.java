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
import ca.phon.app.session.editor.undo.ParticipantUndoableEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.ui.participant.ParticipantEditor;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class EditParticipantAction extends SessionInfoAction {

	private final Participant participant;
	
	private final ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/edit_user", IconSize.SMALL);
	
	public EditParticipantAction(SessionEditor editor,
			SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(SMALL_ICON, ICON);
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

			final EditorEvent<EditorEventType.RecordChangedData> ee =
					new EditorEvent<>(EditorEventType.RecordRefresh, getEditor(),
							new EditorEventType.RecordChangedData(getEditor().getCurrentRecordIndex(), getEditor().currentRecord()));
			getEditor().getEventManager().queueEvent(ee);
		}
	}

}

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

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddParticipantEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.*;
import ca.phon.ui.participant.ParticipantEditor;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class NewParticipantAction extends SessionInfoAction {

	private final static String TXT = "Add participant...";
	
	private final static String DESC = "Add participant to session.";
	
	private final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/add_user", IconSize.SMALL);

	public NewParticipantAction(SessionEditor editor, SessionInfoEditorView view) {
		super(editor, view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant part = factory.createParticipant();
		ParticipantEditor.editNewParticipant(getEditor(), part,
				getEditor().getDataModel().getSession().getDate(),
				getEditor().getDataModel().getSession().getParticipants().otherParticipants(null),
				(wasCanceled) -> {
					if(!wasCanceled) {
						final AddParticipantEdit edit = new AddParticipantEdit(getEditor(), part);
						getEditor().getUndoSupport().postEdit(edit);
					}
				});
	}

}

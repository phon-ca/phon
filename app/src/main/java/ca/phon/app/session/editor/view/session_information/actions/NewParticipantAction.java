/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddParticipantEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.Participant;
import ca.phon.session.SessionFactory;
import ca.phon.ui.participant.ParticipantEditor;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class NewParticipantAction extends SessionInfoAction {

	private static final long serialVersionUID = 993265447147157942L;
	
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
		boolean canceled = ParticipantEditor.editParticipant(getEditor(), part, 
				getEditor().getDataModel().getSession().getDate(),
				getEditor().getDataModel().getSession().getParticipants().otherParticipants(null));

		if(!canceled) {
			final AddParticipantEdit edit = new AddParticipantEdit(getEditor(), part);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}

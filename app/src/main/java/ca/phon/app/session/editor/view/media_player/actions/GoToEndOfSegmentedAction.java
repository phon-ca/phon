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
package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.session.Participant;
import ca.phon.ui.action.PhonActionEvent;

public class GoToEndOfSegmentedAction extends MediaPlayerAction {

	private static final long serialVersionUID = 5084237133652527770L;

	private final static String CMD_NAME = "Go to end of segmented media";
	
	private final static String SHORT_DESC = "Go to end of segmented media";
	
	private final static String CMD_NAME_PART = "Go to end of last segment for ";
	
	private Participant participant;
	
	public GoToEndOfSegmentedAction(SessionEditor editor,
			MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}
	
	public GoToEndOfSegmentedAction(SessionEditor editor,
			MediaPlayerEditorView view, Participant part) {
		super(editor, view);
		
		this.participant = part;
		
		if(this.participant != null) {
			putValue(NAME, CMD_NAME_PART +
					(participant.getName() == null ? participant.getId() : participant.getName()));
		} else {
			putValue(NAME, CMD_NAME);
			putValue(SHORT_DESCRIPTION, SHORT_DESC);
		}
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getMediaPlayerView().onMenuGoto(new PhonActionEvent(e, participant));
	}

}

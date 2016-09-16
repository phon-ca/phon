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
import java.util.List;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.MediaLocationEdit;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class BrowseForMediaAction extends SessionInfoAction {

	private static final long serialVersionUID = 5340838112757528304L;
	
	private final static String TXT = "Browse for media...";
	
	private final static String DESC = "Select media file for session.";
	
	private final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/film-link", IconSize.SMALL);

	public BrowseForMediaAction(SessionEditor editor, SessionInfoEditorView view) {
		super(editor, view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final MediaPlayerEditorView mediaPlayerView = 
				(MediaPlayerEditorView)getEditor().getViewModel()
				.getView(MediaPlayerEditorView.VIEW_TITLE);
		if(mediaPlayerView != null) {
			if(mediaPlayerView.getPlayer() != null && mediaPlayerView.getPlayer().isPlaying())
				mediaPlayerView.getPlayer().pause();
		}
		
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setRunAsync(false);
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(FileFilter.mediaFilter);
		props.setTitle("Select media for session");
		
		final List<String> selectedFiles = 
				NativeDialogs.showOpenDialog(props);
		if(selectedFiles != null && selectedFiles.size() > 0) {
			final String path = selectedFiles.get(0);
			
			final MediaLocationEdit edit = new MediaLocationEdit(getEditor(), path);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}

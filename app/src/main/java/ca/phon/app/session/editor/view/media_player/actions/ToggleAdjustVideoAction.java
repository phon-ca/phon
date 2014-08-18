package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;

public class ToggleAdjustVideoAction extends MediaPlayerAction {
	
private final static String CMD_NAME = "Move media position with record";
	
	private final static String SHORT_DESC = "Move media to beginning of each record's segment";
	
	private static final long serialVersionUID = 3608304092726478707L;

	public ToggleAdjustVideoAction(SessionEditor editor,
			MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		super.getMediaPlayerView().onToggleAdjustVideo();
	}

}

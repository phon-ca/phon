package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.ui.action.PhonActionEvent;

public class TakeSnapshotAction extends MediaPlayerAction {

	private static final long serialVersionUID = 6114592569820589801L;
	
	private final static String CMD_NAME = "Take snapshot...";
	
	private final static String SHORT_DESC = "Save snapshot of video";

	public TakeSnapshotAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getMediaPlayerView().getPlayer().onTakeSnapshot(new PhonActionEvent(e));
	}

}

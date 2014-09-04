package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.session.MediaSegment;
import ca.phon.session.Record;
import ca.phon.session.Tier;
import ca.phon.ui.action.PhonActionEvent;

/**
 * Action for playing current segment in media player.
 */
public class PlaySegmentAction extends MediaPlayerAction {
	
	private static final long serialVersionUID = 8066794189273856660L;

	private final static String CMD_NAME = "Play segment";
	
	private final static String SHORT_DESC = "Play segment for current record";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
	
	public PlaySegmentAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final MediaPlayerEditorView view = getMediaPlayerView();
		view.onPlaySpeakerSegment(new PhonActionEvent(e));
	}

}

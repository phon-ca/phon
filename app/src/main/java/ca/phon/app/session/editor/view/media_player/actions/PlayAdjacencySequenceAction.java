package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.ui.action.PhonActionEvent;

public class PlayAdjacencySequenceAction extends MediaPlayerAction {
	
	private static final long serialVersionUID = 5549269029686145231L;

	private final static String CMD_NAME = "Play adjacency sequence";
	
	private final static String SHORT_DESC = "";
	
	private final static String ICON = "";
	
	private final static KeyStroke KS = 
			KeyStroke.getKeyStroke(KeyEvent.VK_R, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK);
	
	public PlayAdjacencySequenceAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final MediaPlayerEditorView view = getMediaPlayerView();
		view.onPlayConvPeriod();
	}

}

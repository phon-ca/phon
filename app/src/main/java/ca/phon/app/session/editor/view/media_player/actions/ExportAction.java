package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.ui.action.PhonActionEvent;

public class ExportAction extends MediaPlayerAction {

	private static final long serialVersionUID = -2439537343549469610L;
	
	private final static String CMD_NAME = "Export media...";
	
	private final static String SHORT_DESC = "";
	
	private final static String ICON = "";

	public ExportAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
//		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MediaPlayerEditorView view = getMediaPlayerView();
		view.onExportMedia(new PhonActionEvent(e));
	}

}

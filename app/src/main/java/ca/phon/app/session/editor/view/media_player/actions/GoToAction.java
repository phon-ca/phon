package ca.phon.app.session.editor.view.media_player.actions;

import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.media_player.MediaPlayerEditorView;
import ca.phon.ui.action.PhonActionEvent;

public class GoToAction extends MediaPlayerAction {
	
	private final static Logger LOGGER = Logger
			.getLogger(GoToAction.class.getName());

	private static final long serialVersionUID = -2265485841201934953L;
	
	private final static String CMD_NAME = "Go to...";
	
	private final static String SHORT_DESC = "Go to a specific time";

	public GoToAction(SessionEditor editor, MediaPlayerEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			getMediaPlayerView().onMenuSelectGoto(new PhonActionEvent(e));
		} catch (ParseException e1) {
			LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
	}

}

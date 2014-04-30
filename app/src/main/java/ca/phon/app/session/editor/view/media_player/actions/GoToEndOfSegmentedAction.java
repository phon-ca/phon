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
	public void actionPerformed(ActionEvent e) {
		getMediaPlayerView().onMenuGoto(new PhonActionEvent(e, participant));
	}

}

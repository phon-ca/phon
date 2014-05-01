package ca.phon.app.session.editor.view.waveform.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.waveform.WaveformEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class PlayAction extends WaveformEditorViewAction {

	private static final long serialVersionUID = 3972276703916486391L;
	
	private final static String CMD_NAME = "Play";
	
	private final static String SHORT_DESC = "Play segment/selection";
	
	private final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);

	public PlayAction(SessionEditor editor, WaveformEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().play();
	}

}

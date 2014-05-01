package ca.phon.app.session.editor.view.waveform.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.waveform.WaveformEditorView;

public class GenerateAction extends WaveformEditorViewAction {

	private static final long serialVersionUID = 5347314784532832778L;
	
	private final static String CMD_NAME = "Generate wav";
	
	private final static String SHORT_DESC = "Generate wav file from session media";

	public GenerateAction(SessionEditor editor, WaveformEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().generateAudioFile();
	}

}

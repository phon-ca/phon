package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;

public class GenerateAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 5347314784532832778L;
	
	private final static String CMD_NAME = "Generate wav";
	
	private final static String SHORT_DESC = "Generate wav file from session media";

	public GenerateAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().generateAudioFile();
	}

}

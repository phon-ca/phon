package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;

public class ShowMoreAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 5911882184540602671L;
	
	private final static String CMD_NAME = "Show more";
	
	private final static String SHORT_DESC = "Increase length of audio data displayed";

	public ShowMoreAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().showMore();
	}

}

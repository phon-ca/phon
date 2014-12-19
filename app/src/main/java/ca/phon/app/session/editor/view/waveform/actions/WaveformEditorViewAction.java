package ca.phon.app.session.editor.view.waveform.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;

public abstract class WaveformEditorViewAction extends SessionEditorAction {

	private static final long serialVersionUID = 3163798645290753856L;

	private final SpeechAnalysisEditorView view;

	public WaveformEditorViewAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor);
		this.view = view;
	}

	public SpeechAnalysisEditorView getView() {
		return this.view;
	}
	
}

package ca.phon.app.session.editor.view.waveform.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.waveform.WaveformEditorView;

public abstract class WaveformEditorViewAction extends SessionEditorAction {

	private static final long serialVersionUID = 3163798645290753856L;

	private final WaveformEditorView view;

	public WaveformEditorViewAction(SessionEditor editor, WaveformEditorView view) {
		super(editor);
		this.view = view;
	}

	public WaveformEditorView getView() {
		return this.view;
	}
	
}

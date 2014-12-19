package ca.phon.app.session.editor.view.waveform.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveAction extends WaveformEditorViewAction {

	private static final long serialVersionUID = -8249184983712169161L;

	private final static String CMD_NAME = "Save...";
	
	private final static String SHORT_DESC = "Save segment/selection";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/filesave", IconSize.SMALL);
	
	public SaveAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().onExport();
	}

}

package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class GenerateAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 5347314784532832778L;
	
	private final static String CMD_NAME = "Generate wav";
	
	private final static String SHORT_DESC = "Generate wav file from session media";
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("misc/oscilloscope", IconSize.SMALL);

	public GenerateAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().generateAudioFile();
	}

}

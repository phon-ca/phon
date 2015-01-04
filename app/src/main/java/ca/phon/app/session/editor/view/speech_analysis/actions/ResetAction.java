package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisTier;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ResetAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 2541481642552447379L;
	
	private final static String CMD_NAME = "Reset";
	
	private final static String SHORT_DESC = "Fit segement to view";
	
	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);

	public ResetAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		super(editor, view);
		
		putValue(NAME, CMD_NAME);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().update();
		for(SpeechAnalysisTier tier:getView().getPluginTiers()) {
			tier.onRefresh(); 
		}
	}

}

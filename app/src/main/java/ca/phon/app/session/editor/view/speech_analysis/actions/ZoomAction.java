package ca.phon.app.session.editor.view.speech_analysis.actions;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.media.sampled.PCMSegmentView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ZoomAction extends SpeechAnalysisEditorViewAction {

	private static final long serialVersionUID = 5911882184540602671L;
	
	private final static String CMD_NAME_ZOOMIN = "Zoom in";
	
	private final static String CMD_NAME_ZOOMOUT = "Zoom out";
	
	private final static ImageIcon ZOOMIN_ICON = 
			IconManager.getInstance().getIcon("actions/zoom-in-3", IconSize.SMALL);
	
	private final static ImageIcon ZOOMOUT_ICON =
			IconManager.getInstance().getIcon("actions/zoom-out-3", IconSize.SMALL);
	
	private boolean zoomIn = true;

	public ZoomAction(SessionEditor editor, SpeechAnalysisEditorView view) {
		this(editor, view, true);
	}
	
	public ZoomAction(SessionEditor editor, SpeechAnalysisEditorView view, boolean zoomIn) {
		super(editor, view);
		
		this.zoomIn = zoomIn;
		putValue(NAME, (zoomIn ? CMD_NAME_ZOOMIN : CMD_NAME_ZOOMOUT));
		putValue(SMALL_ICON, (zoomIn ? ZOOMIN_ICON : ZOOMOUT_ICON));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final PCMSegmentView wavDisplay = getView().getWavDisplay();
		float currentWindow = wavDisplay.getWindowLength();
		if(zoomIn) {
			if(currentWindow > 0.1f) {
				currentWindow -= 0.1f;
			}
			if(currentWindow < 0.1f) {
				currentWindow = 0.1f;
			}
		} else {
			if(currentWindow < wavDisplay.getSampled().getLength()) {
				currentWindow += 0.1;
			}
			if(currentWindow + wavDisplay.getWindowStart() > wavDisplay.getSampled().getLength()) {
				wavDisplay.setWindowStart(wavDisplay.getSampled().getLength() - currentWindow);
			}
		}
		wavDisplay.setWindowLength(currentWindow);
	}

}

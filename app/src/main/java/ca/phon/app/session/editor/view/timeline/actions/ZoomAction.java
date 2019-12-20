package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.session.editor.view.speech_analysis.SpeechAnalysisEditorView;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ZoomAction extends TimelineAction {

	private static final long serialVersionUID = 1L;
	
	private final static String CMD_NAME_ZOOMIN = "Zoom in";
	
	private final static String CMD_NAME_ZOOMOUT = "Zoom out";
	
	private final static ImageIcon ZOOMIN_ICON = 
			IconManager.getInstance().getIcon("actions/zoom-in-3", IconSize.SMALL);
	
	private final static ImageIcon ZOOMOUT_ICON =
			IconManager.getInstance().getIcon("actions/zoom-out-3", IconSize.SMALL);
	
	private final static String ZOOM_AMOUNT_PROP = 
			TimelineView.class.getName() + ".zoomAmount";
	private final static float DEFAULT_ZOOM_AMOUNT = 0.3f;
	private float zoomAmount = PrefHelper.getFloat(ZOOM_AMOUNT_PROP, DEFAULT_ZOOM_AMOUNT);
	
	private boolean zoomIn = true;
	
	private final static float MIN_PXPERS = 1.0f;
	
	private final static float MAX_PXPERS = 3200.0f;

	public ZoomAction(TimelineView view, boolean zoomIn) {
		super(view);
		
		this.zoomIn = zoomIn;
		
		putValue(NAME, (zoomIn ? CMD_NAME_ZOOMIN : CMD_NAME_ZOOMOUT));
		putValue(SMALL_ICON, (zoomIn ? ZOOMIN_ICON : ZOOMOUT_ICON));
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		TimeUIModel timeModel = getView().getTimeModel();
		
		float pxPerS = timeModel.getPixelsPerSecond();
		float zoomAmount = -1.0f * pxPerS * this.zoomAmount;
		if(zoomIn) {
			zoomAmount *= -1.0f;
		}
		pxPerS += zoomAmount;
		
		pxPerS = Math.max(MIN_PXPERS, Math.min(pxPerS, MAX_PXPERS));
		
		// beep to indicate we are at a limit
		if(pxPerS == MIN_PXPERS || pxPerS == MAX_PXPERS)
			Toolkit.getDefaultToolkit().beep();
		
		timeModel.setPixelsPerSecond(pxPerS);
	}

}

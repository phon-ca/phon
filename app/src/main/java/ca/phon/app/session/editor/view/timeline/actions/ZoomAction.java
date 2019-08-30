package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ZoomAction extends TimelineAction {

	private static final long serialVersionUID = 1L;
	
	private final static String TXT = "Zoom";
	
	private int direction = 1;
	
	private final static ImageIcon ZOOMIN_ICON = 
			IconManager.getInstance().getIcon("actions/zoom-in-3", IconSize.SMALL);
	
	private final static ImageIcon ZOOMOUT_ICON =
			IconManager.getInstance().getIcon("actions/zoom-out-3", IconSize.SMALL);

	public ZoomAction(TimelineView view, int direction) {
		super(view);
		
		this.direction = direction;
		
		String name = TXT + (direction < 0 ? " out" : " in");
		putValue(NAME, name);
		
		Icon icon = (direction < 0 ? ZOOMOUT_ICON : ZOOMIN_ICON);
		putValue(SMALL_ICON, icon);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		int currentZoomIdx = 
				Arrays.binarySearch(TimelineView.zoomValues, getView().getTimeModel().getPixelsPerSecond());
		
		int newZoomIdx = currentZoomIdx + direction;
		if(newZoomIdx < 0)
			newZoomIdx = 0;
		if(newZoomIdx >= TimelineView.zoomValues.length)
			newZoomIdx = TimelineView.zoomValues.length - 1;
		
		float currentViewStartTime = getView().getTimeModel().timeAtX(
				getView().getRecordTier().getVisibleRect().getX());
		getView().getTimeModel().setPixelsPerSecond(TimelineView.zoomValues[newZoomIdx]);
		getView().scrollToTime(currentViewStartTime);
	}

}

package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.SwingConstants;

import ca.phon.app.session.editor.view.timeline.TimelineView;

public class ZoomAction extends TimelineAction {

	private static final long serialVersionUID = 1L;
	
	private final static String TXT = "Zoom";
	
	private int direction = 1;

	public ZoomAction(TimelineView view, int direction) {
		super(view);
		
		this.direction = direction;
		
		String name = TXT + (direction < 0 ? " out" : " in");
		putValue(NAME, name);
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
		getView().getTimeModel().setPixelsPerSecond(TimelineView.zoomValues[newZoomIdx]);
	}

}

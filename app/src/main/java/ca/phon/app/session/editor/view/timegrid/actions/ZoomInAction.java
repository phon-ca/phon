package ca.phon.app.session.editor.view.timegrid.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.view.timegrid.TimeGridView;

public class ZoomInAction extends TimeGridAction {

	private static final long serialVersionUID = 1L;
	
	private final static String TXT = "Zoom In";

	public ZoomInAction(TimeGridView view) {
		super(view);
		
		putValue(NAME, TXT);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getView().getTimebarModel().setPixelsPerSecond(
				getView().getTimebarModel().getPixelsPerSecond() + 10.0f);
	}

}

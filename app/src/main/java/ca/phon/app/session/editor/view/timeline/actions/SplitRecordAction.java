package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.view.timeline.TimelineView;

/**
 * Split current record interval.
 * 
 */
public class SplitRecordAction extends TimelineAction {
	
	public final static String TXT = "Split record";
	
	public final static String DESC = "Split record into two";
	
	public SplitRecordAction(TimelineView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getView().getRecordTier().beginSplitMode();
	}
	
}

package ca.phon.app.session.editor.view.timeline.actions;

import java.awt.event.*;

import ca.phon.app.session.editor.view.timeline.*;
import ca.phon.util.icons.*;

/**
 * Split current record interval.
 * 
 */
public class SplitRecordAction extends TimelineAction {
	
	public final static String TXT = "Split record";
	
	public final static String DESC = "Split record into two";
	
	public final static String ICON = "actions/group_split";
	
	public SplitRecordAction(TimelineView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, IconManager.getInstance().getIcon(ICON, IconSize.SMALL));
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		getView().getRecordTier().beginSplitMode();
	}
	
}

package ca.phon.app.session.editor.view.timeline.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.view.timeline.TimelineView;

public abstract class TimelineAction extends HookableAction {

	private TimelineView timelineView;
	
	public TimelineAction(TimelineView view) {
		super();
		
		this.timelineView = view;
	}
	
	public TimelineView getView() {
		return this.timelineView;
	}
	
}

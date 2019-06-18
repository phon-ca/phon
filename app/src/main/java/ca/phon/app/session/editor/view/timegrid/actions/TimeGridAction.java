package ca.phon.app.session.editor.view.timegrid.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.view.timegrid.TimeGridView;

public abstract class TimeGridAction extends HookableAction {

	private TimeGridView timeGridView;
	
	public TimeGridAction(TimeGridView view) {
		super();
		
		this.timeGridView = view;
	}
	
	public TimeGridView getView() {
		return this.timeGridView;
	}
	
}

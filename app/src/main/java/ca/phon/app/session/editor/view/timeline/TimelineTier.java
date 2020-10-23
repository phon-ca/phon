package ca.phon.app.session.editor.view.timeline;

import ca.phon.app.session.editor.*;
import ca.phon.media.*;
import ca.phon.ui.menu.*;

public abstract class TimelineTier extends TimeComponent {

	private static final long serialVersionUID = 1L;

	public final TimelineView parentView;
	
	public TimelineTier(TimelineView parent) {
		super(parent.getTimeModel());
		
		this.parentView = parent;
	}

	public TimelineView getParentView() {
		return this.parentView;
	}
	
	public boolean isResizeable() {
		return true;
	}
	
	/**
	 * Setup context menu
	 */
	public abstract void setupContextMenu(MenuBuilder builder, boolean includeAccelerators);
	
	/**
	 * Called when the {@link EditorView} is closed
	 * 
	 */
	public abstract void onClose();
	
}

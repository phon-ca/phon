package ca.phon.app.session.editor.view.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ca.phon.app.media.TimeComponent;
import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.EditorView;
import ca.phon.ui.menu.MenuBuilder;

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

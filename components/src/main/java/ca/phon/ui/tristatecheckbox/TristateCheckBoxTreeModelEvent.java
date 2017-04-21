/*
 * 
 */
package ca.phon.ui.tristatecheckbox;

import javax.swing.event.TreeModelEvent;

/**
 * {@link TreeModelEvent} sent when changes to checking paths for a node changes.
 * 
 *
 */
public class TristateCheckBoxTreeModelEvent extends TreeModelEvent {
	
	private static final long serialVersionUID = -1250202341580525562L;

	private TristateCheckBoxState state;
	
	public TristateCheckBoxTreeModelEvent(Object source, Object[] path, TristateCheckBoxState state) {
		this(source, path, null, null, state);
	}
	
	public TristateCheckBoxTreeModelEvent(Object source, Object[] path, int[] childIndices, Object[] children, TristateCheckBoxState state) {
		super(source, path, childIndices, children);
		this.state = state;
	}
	

	public TristateCheckBoxState getState() {
		return this.state;
	}
	
}

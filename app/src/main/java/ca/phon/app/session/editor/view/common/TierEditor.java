package ca.phon.app.session.editor.view.common;

import java.util.List;

import javax.swing.JComponent;

/**
 * Interface used to load tier editors.
 */
public interface TierEditor {

	/**
	 * Get the editor component
	 * 
	 * @return component
	 */
	public JComponent getEditorComponent();
	
	/**
	 * Add tier editor listener
	 */
	public void addTierEditorListener(TierEditorListener listener);
	
	/**
	 * remove tier editor listener
	 */
	public void removeTierEditorListener(TierEditorListener listener);
	
	/**
	 * Get tier editor listeners
	 */
	public List<TierEditorListener> getTierEditorListeners();
	
}

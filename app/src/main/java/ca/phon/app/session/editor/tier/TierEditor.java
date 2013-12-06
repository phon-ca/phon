package ca.phon.app.session.editor.tier;

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
	
}

package ca.phon.app.session.editor.view.common;

import ca.phon.session.Tier;

/**
 * Listener interface for tier editors.
 *
 */
public interface TierEditorListener {
	
	/**
	 * Called when the value of a tier changes.
	 * 
	 * @param tier
	 * @param groupIndex
	 * @param newValue
	 * @param oldValue
	 */
	public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue, T oldValue);
	
	/**
	 * Called when the value of a tier has changed and focus has left the tier editor.
	 * 
	 * @param tier
	 * @param groupIndex
	 * @param newValue
	 * @param oldValue
	 */
	public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue, T oldValue);

}

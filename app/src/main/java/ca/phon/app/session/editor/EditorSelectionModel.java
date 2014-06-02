package ca.phon.app.session.editor;

import java.util.List;

/**
 * Selection model for the {@link SessionEditor}.
 */
public interface EditorSelectionModel {
	
	/**
	 * Get a list of all current selections.
	 * 
	 * @return list of selections
	 */
	public List<SessionEditorSelection> getSelections();
	
	/**
	 * Get the first selection in the list
	 * 
	 * @return first selection, or <code>null</code> if no selections
	 *  are available
	 */
	public SessionEditorSelection getFirstSelection();
	
	/**
	 * Get the last selection in the list
	 * 
	 * @return last selection, or <code>null</code> if no selections
	 *  are available
	 */
	public SessionEditorSelection getLastSelection();
	
	/**
	 * Clear all selections
	 * 
	 * 
	 */
	public void clear();
	
	/**
	 * Set the current selection.  This method will clear
	 * all other selections.
	 * 
	 * @param selection
	 * 
	 */
	public void setSelection(SessionEditorSelection selection);
	
	/**
	 * Add the given selection to the list of current selections.
	 * 
	 * @param selection
	 */
	public void addSelection(SessionEditorSelection selection);
	
	/**
	 * Get selections for the specified record.
	 * 
	 * @param recordIndex
	 * 
	 * @return selections for record index
	 */
	public List<SessionEditorSelection> getSelectionsForRecord(int recordIndex);
	
	public List<SessionEditorSelection> getSelectionsForTier(int recordIndex, String tierName);
	
	public List<SessionEditorSelection> getSelectionsForGroup(int recordIndex, String tierName, int groupIndex);
	
	/**
	 * Add a listener for this model.
	 * 
	 * @param listener
	 */
	public void addSelectionModelListener(EditorSelectionModelListener listener);
	
	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public void removeSelectionModelListener(EditorSelectionModelListener listener);
	
	/**
	 * Get all listeners
	 *
	 * @return listeners
	 */
	public List<EditorSelectionModelListener> getSelectionModelListeners();
	
}

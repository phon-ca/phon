/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

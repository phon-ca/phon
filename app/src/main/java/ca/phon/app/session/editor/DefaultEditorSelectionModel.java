/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultEditorSelectionModel implements EditorSelectionModel {
	
	private final List<SessionEditorSelection> selections =
			Collections.synchronizedList(new ArrayList<SessionEditorSelection>());

	@Override
	public List<SessionEditorSelection> getSelections() {
		return Collections.unmodifiableList(selections);
	}

	@Override
	public SessionEditorSelection getFirstSelection() {
		return (selections.size() > 0 ? selections.get(0) : null);
	}

	@Override
	public SessionEditorSelection getLastSelection() {
		return (selections.size() > 0 ? selections.get(selections.size()-1) : null);
	}

	@Override
	public void clear() {
		selections.clear();
		fireSelectionsCleared();
	}

	@Override
	public void setSelection(SessionEditorSelection selection) {
		selections.clear();
		selections.add(selection);
		fireSelectionSet(selection);
	}

	@Override
	public void addSelection(SessionEditorSelection selection) {
		selections.add(selection);
		fireSelectionAdded(selection);
	}
	
	private final List<EditorSelectionModelListener> listeners = 
			Collections.synchronizedList(new ArrayList<EditorSelectionModelListener>());

	@Override
	public void addSelectionModelListener(EditorSelectionModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSelectionModelListener(
			EditorSelectionModelListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public List<SessionEditorSelection> getSelectionsForRecord(int recordIndex) {
		return getSelectionsForTier(recordIndex, null);
	}
	
	@Override
	public List<SessionEditorSelection> getSelectionsForTier(int recordIndex, String tierName) {
		return getSelectionsForGroup(recordIndex, tierName, -1);
	}

	@Override
	public List<SessionEditorSelection> getSelectionsForGroup(int recordIndex, String tierName, int groupIndex) {
		final List<SessionEditorSelection> retVal = new ArrayList<SessionEditorSelection>();
		for(SessionEditorSelection selection:getSelections()) {
			boolean keep = true;
			
			if(recordIndex >= 0) {
				keep &= selection.getRecordIndex() == recordIndex;
			}
			
			if(tierName != null) {
				keep &= selection.getTierName().equals(tierName);
			}
			
			if(groupIndex >= 0) {
				keep &= selection.getGroupIndex() == groupIndex;
			}
			
			if(keep) {
				retVal.add(selection);
			}
		}
		return retVal;
	}

	@Override
	public List<EditorSelectionModelListener> getSelectionModelListeners() {
		return Collections.unmodifiableList(listeners);
	}
	
	public void fireSelectionAdded(SessionEditorSelection selection) {
		for(EditorSelectionModelListener listener:getSelectionModelListeners()) {
			listener.selectionAdded(this, selection);
		}
	}

	public void fireSelectionSet(SessionEditorSelection selection) {
		for(EditorSelectionModelListener listener:getSelectionModelListeners()) {
			listener.selectionSet(this, selection);
		}
	}
	
	public void fireSelectionsCleared() {
		for(EditorSelectionModelListener listener:getSelectionModelListeners()) {
			listener.selectionsCleared(this);
		}
	}
	
}

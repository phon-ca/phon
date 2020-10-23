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

import java.util.*;

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

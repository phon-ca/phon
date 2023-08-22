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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;

import java.util.*;

public class TierViewItemEdit extends SessionUndoableEdit {

	private final TierViewItem oldItem;
	
	private final TierViewItem newItem;

	public TierViewItemEdit(SessionEditor editor, TierViewItem oldItem, TierViewItem newItem) {
		this(editor.getSession(), editor.getEventManager(), oldItem, newItem);
	}

	public TierViewItemEdit(Session session, EditorEventManager editorEventManager, TierViewItem oldItem, TierViewItem newItem) {
		super(session, editorEventManager);
		this.oldItem = oldItem;
		this.newItem = newItem;
	}

	private List<EditorEventType.TierViewChangeType> calculateChanges(TierViewItem newItem, TierViewItem oldItem) {
		final List<EditorEventType.TierViewChangeType> changes = new ArrayList<>();
		if (!newItem.getTierFont().equals(oldItem.getTierFont())) {
			changes.add(EditorEventType.TierViewChangeType.TIER_FONT_CHANGE);
		} else if(!newItem.getTierName().equals(oldItem.getTierName())) {
			changes.add(EditorEventType.TierViewChangeType.TIER_NAME_CHANGE);
		} else if(newItem.isTierLocked() != oldItem.isTierLocked()) {
			changes.add(newItem.isTierLocked() ? EditorEventType.TierViewChangeType.LOCK_TIER : EditorEventType.TierViewChangeType.UNLOCK_TIER);
		} else if(newItem.isVisible() != oldItem.isVisible()) {
			changes.add(newItem.isVisible() ? EditorEventType.TierViewChangeType.SHOW_TIER : EditorEventType.TierViewChangeType.HIDE_TIER);
		}
		return changes;
	}

	private void fireChangeEvents(List<TierViewItem> tierView, List<TierViewItem> oldView, int idx) {
		final List<EditorEventType.TierViewChangeType> changes = calculateChanges(tierView.get(idx), oldView.get(idx));
		for(EditorEventType.TierViewChangeType change:changes) {
			final EditorEvent<EditorEventType.TierViewChangedData> ee =
					new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(oldView, tierView, change,
							List.of(newItem.getTierName()), List.of(getSession().getTierView().indexOf(newItem))));
			getEditorEventManager().queueEvent(ee);
		}
	}
	
	@Override
	public void undo() {
		final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
		final List<TierViewItem> tierView = new ArrayList<>(oldView);
		final int idx = tierView.indexOf(newItem);
		tierView.remove(idx);
		tierView.add(idx, oldItem);
		getSession().setTierView(tierView);
		fireChangeEvents(tierView, oldView, idx);
	}

	@Override
	public void doIt() {
		final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
        final List<TierViewItem> tierView = new ArrayList<>(oldView);
		final int idx = tierView.indexOf(oldItem);
		tierView.remove(idx);
		tierView.add(idx, newItem);
		getSession().setTierView(tierView);
		fireChangeEvents(tierView, oldView, idx);
	}

}

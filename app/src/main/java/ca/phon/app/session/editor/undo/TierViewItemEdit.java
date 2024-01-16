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
import ca.phon.session.*;
import ca.phon.session.Record;

import java.util.*;

public class TierViewItemEdit extends SessionUndoableEdit {

	private final TierViewItem oldItem;
	
	private final TierViewItem newItem;

	private final TierDescription oldTierDescription;

	private final TierDescription newTierDescription;

	public TierViewItemEdit(SessionEditor editor, TierViewItem oldItem, TierViewItem newItem) {
		this(editor.getSession(), editor.getEventManager(), oldItem, newItem);
	}

	public TierViewItemEdit(SessionEditor editor, TierViewItem oldItem, TierViewItem newItem,
							TierDescription oldTierDescription, TierDescription tierDescription) {
		this(editor.getSession(), editor.getEventManager(), oldItem, newItem, oldTierDescription, tierDescription);
	}

	public TierViewItemEdit(Session session, EditorEventManager editorEventManager, TierViewItem oldItem, TierViewItem newItem) {
		this(session, editorEventManager, oldItem, newItem, null, null);
	}

	public TierViewItemEdit(Session session, EditorEventManager editorEventManager, TierViewItem oldItem, TierViewItem newItem,
							TierDescription oldTierDescription, TierDescription tierDescription) {
		super(session, editorEventManager);
		this.oldItem = oldItem;
		this.newItem = newItem;
		this.oldTierDescription = oldTierDescription;
		this.newTierDescription = tierDescription;
	}

	private List<EditorEventType.TierViewChangeType> calculateChanges(TierViewItem oldItem, TierViewItem newItem, TierDescription oldTierDescription, TierDescription newTierDescription) {
		final List<EditorEventType.TierViewChangeType> changes = new ArrayList<>();
		if (!newItem.getTierFont().equals(oldItem.getTierFont())) {
			changes.add(EditorEventType.TierViewChangeType.TIER_FONT_CHANGE);
		}
		if(!newItem.getTierName().equals(oldItem.getTierName())) {
			changes.add(EditorEventType.TierViewChangeType.TIER_NAME_CHANGE);
		}
		if(newItem.isTierLocked() != oldItem.isTierLocked()) {
			changes.add(newItem.isTierLocked() ? EditorEventType.TierViewChangeType.LOCK_TIER : EditorEventType.TierViewChangeType.UNLOCK_TIER);
		}
		if(newItem.isVisible() != oldItem.isVisible()) {
			changes.add(newItem.isVisible() ? EditorEventType.TierViewChangeType.SHOW_TIER : EditorEventType.TierViewChangeType.HIDE_TIER);
		}

		if(oldTierDescription != null && newTierDescription != null) {
			if(oldTierDescription.isExcludeFromAlignment() != newTierDescription.isExcludeFromAlignment()) {
				changes.add(EditorEventType.TierViewChangeType.ALIGNED_TIER);
			}
			if(oldTierDescription.getDeclaredType() != newTierDescription.getDeclaredType()) {
				changes.add(EditorEventType.TierViewChangeType.TIER_TYPE);
			}
			if(oldTierDescription.isBlind() != newTierDescription.isBlind()) {
				changes.add(EditorEventType.TierViewChangeType.BLIND_TIER);
			}
		}

		return changes;
	}

	private void fireChangeEvents(List<EditorEventType.TierViewChangeType> changes, List<TierViewItem> tierView, List<TierViewItem> oldView, int idx) {
		for(EditorEventType.TierViewChangeType change:changes) {
			final EditorEvent<EditorEventType.TierViewChangedData> ee =
					new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(oldView, tierView, change,
							List.of(newItem.getTierName()), List.of(getSession().getTierView().indexOf(newItem))));
			getEditorEventManager().queueEvent(ee);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void changeTierNames(Session session, String oldName, String newName) {
		// change tier name in records
		for(Record r:getSession().getRecords()) {
			if(r.hasTier(oldName)) {
				final Tier<?> oldTier = r.getTier(oldName);
				r.removeTier(oldName);

				final Tier<?> newTier = SessionFactory.newFactory().createTier(newName, oldTier.getDeclaredType(), oldTier.getTierParameters(), oldTier.isExcludeFromAlignment());
				newTier.setText(oldTier.toString());
				r.putTier(newTier);
			}
		}
	}

	private void changeTierType(Session session, String tierName, TierDescription tierDescription, Class<?> newType) {
		// change tier type in records
		for(Record r:getSession().getRecords()) {
			if(r.hasTier(tierName)) {
				final Tier<?> oldTier = r.getTier(tierName);
				r.removeTier(tierDescription.getName());

				final Tier<?> newTier = SessionFactory.newFactory().createTier(tierDescription);
				newTier.setText(oldTier.toString());
				r.putTier(newTier);
			}
		}
	}
	
	@Override
	public void undo() {
		final List<EditorEventType.TierViewChangeType> changes = calculateChanges(newItem, oldItem, newTierDescription, oldTierDescription);
		final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
		final List<TierViewItem> tierView = new ArrayList<>(oldView);
		final int idx = tierView.indexOf(newItem);
		if(changes.contains(EditorEventType.TierViewChangeType.TIER_NAME_CHANGE)) {
			// we need to change the tier name in all tiers in the session
			changeTierNames(getSession(), newItem.getTierName(), oldItem.getTierName());
		}
		if(oldTierDescription != null && newTierDescription != null) {
			// replace tier description
			for(int i = 0; i < getSession().getUserTierCount(); i++) {
				final TierDescription td = getSession().getUserTier(i);

				if(td == newTierDescription) {
					getSession().removeUserTier(td);
					getSession().addUserTier(i, oldTierDescription);
				}
			}
			if(changes.contains(EditorEventType.TierViewChangeType.TIER_TYPE)) {
				changeTierType(getSession(), newItem.getTierName(), oldTierDescription, oldTierDescription.getDeclaredType());
			}
			if(changes.contains(EditorEventType.TierViewChangeType.BLIND_TIER)) {
				if(!oldTierDescription.isBlind()) {
					// remove blind tier
					getSession().removeUserTier(oldTierDescription);
				} else {
					// add blind tier
					getSession().addUserTier(oldTierDescription);
				}
			}
		}
		tierView.remove(idx);
		tierView.add(idx, oldItem);
		getSession().setTierView(tierView);
		fireChangeEvents(changes, tierView, oldView, idx);
	}

	@Override
	public void doIt() {
		final List<EditorEventType.TierViewChangeType> changes = calculateChanges(oldItem, newItem, oldTierDescription, newTierDescription);
		final List<TierViewItem> oldView = new ArrayList<>(getSession().getTierView());
        final List<TierViewItem> tierView = new ArrayList<>(oldView);

		if (changes.contains(EditorEventType.TierViewChangeType.TIER_NAME_CHANGE)) {
			// we need to change the tier name in all tiers in the session
			changeTierNames(getSession(), oldItem.getTierName(), newItem.getTierName());
		}
		if(oldTierDescription != null && newTierDescription != null) {
			// replace tier description
			for (int i = 0; i < getSession().getUserTierCount(); i++) {
				final TierDescription td = getSession().getUserTier(i);

				if (td == oldTierDescription) {
					getSession().removeUserTier(td);
					getSession().addUserTier(i, newTierDescription);
				}
			}
			if(changes.contains(EditorEventType.TierViewChangeType.TIER_TYPE)) {
				changeTierType(getSession(), oldTierDescription.getName(), newTierDescription, newTierDescription.getDeclaredType());
			}
			if(changes.contains(EditorEventType.TierViewChangeType.BLIND_TIER)) {
				if(!newTierDescription.isBlind()) {
					// remove blind tier
					getSession().removeUserTier(newTierDescription);
				} else {
					// add blind tier
					getSession().addUserTier(newTierDescription);
				}
			}
		}

		final int idx = tierView.indexOf(oldItem);
		tierView.remove(idx);
		tierView.add(idx, newItem);
		getSession().setTierView(tierView);

		fireChangeEvents(changes, tierView, oldView, idx);
	}

}

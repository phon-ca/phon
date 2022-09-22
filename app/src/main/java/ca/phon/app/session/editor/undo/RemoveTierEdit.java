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
import ca.phon.session.Record;
import ca.phon.session.*;

import javax.swing.undo.CannotUndoException;
import java.util.*;

public class RemoveTierEdit extends SessionEditorUndoableEdit {

	private final TierDescription tierDescription;
	
	private int tierIdx = -1;
	
	private final TierViewItem tierViewItem;
	
	private int tierViewIdx = -1;
	
	private Map<UUID, Tier<?>> tierMap = new HashMap<>();

	public RemoveTierEdit(SessionEditor editor, TierDescription tierDesc,
			TierViewItem tvi) {
		super(editor);
		
		this.tierDescription = tierDesc;
		this.tierViewItem = tvi;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo delete tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo delete tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		Session session = getEditor().getSession();
		
		if(tierIdx >= 0)
			session.addUserTier(tierIdx, tierDescription);
		else
			session.addUserTier(tierDescription);

		final List<TierViewItem> oldTierView = session.getTierView();
		final List<TierViewItem> tierView = new ArrayList<>(oldTierView);
		if(tierViewIdx >= 0) {
			tierView.add(tierViewIdx, tierViewItem);
		} else {
			tierView.add(tierViewItem);
		}
		session.setTierView(tierView);
		
		for(Record r:session.getRecords()) {
			Tier<?> tier = tierMap.get(r.getUuid());
			if(tier != null) {
				r.putTier(tier);
			}
		}

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(oldTierView, tierView));
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		Session session = getEditor().getSession();

		tierMap.clear();
		for(Record r:session.getRecords()) {
			if(r.hasTier(tierDescription.getName())) {
				tierMap.put(r.getUuid(), r.getTier(tierDescription.getName()));
				r.removeTier(tierDescription.getName());
			}
		}
		
		for(int i = 0; i < session.getUserTierCount(); i++) {
			if(session.getUserTier(i) == tierDescription) {
				tierIdx = i;
				break;
			}
		}
		session.removeUserTier(tierIdx);

		final List<TierViewItem> oldTierView = session.getTierView();
		final List<TierViewItem> tierView = new ArrayList<>(oldTierView);
		tierViewIdx = tierView.indexOf(this.tierViewItem);
		tierView.remove(this.tierViewItem);
		session.setTierView(tierView);

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(oldTierView, tierView));
		getEditor().getEventManager().queueEvent(ee);
	}
	
}

/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.util.*;

import javax.swing.undo.*;

import ca.phon.app.session.editor.*;
import ca.phon.session.*;
import ca.phon.session.Record;

public class RemoveTierEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 5829729907299422281L;
	
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
		
		final Object oldSource = getSource();
		setSource(getEditor().getUndoSupport());
		
		if(tierIdx >= 0)
			session.addUserTier(tierIdx, tierDescription);
		else
			session.addUserTier(tierDescription);
		
		List<TierViewItem> tierView = new ArrayList<>(session.getTierView());
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

		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), tierView);
		
		setSource(oldSource);
	}

	@Override
	public void doIt() {
		Session session = getEditor().getSession();
		
		final Object oldSource = getSource();
		setSource(getEditor().getUndoSupport());
		
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
		
		List<TierViewItem> tierView = new ArrayList<>(session.getTierView());
		tierViewIdx = tierView.indexOf(this.tierViewItem);
		tierView.remove(this.tierViewItem);
		session.setTierView(tierView);
		
		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), tierView);
		
		setSource(oldSource);
	}
	
}

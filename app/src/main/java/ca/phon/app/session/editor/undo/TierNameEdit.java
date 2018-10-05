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

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

public class TierNameEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 1803430040195263868L;
	
	private final String newTierName;
	
	private final String oldTierName;

	public TierNameEdit(SessionEditor editor, String newTierName, String oldTierName) {
		super(editor);
		this.newTierName = newTierName;
		this.oldTierName = oldTierName;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo change tier name";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo change tier name";
	}

	@Override
	public void undo() throws CannotUndoException {
		final Object oldSource = getSource();
		setSource(getEditor().getUndoSupport());
		changeTierName(newTierName, oldTierName);
		setSource(oldSource);
	}

	@Override
	public void doIt() {
		changeTierName(oldTierName, newTierName);
	}

	private void changeTierName(String tierName, String newTierName) {
		final SessionFactory factory = SessionFactory.newFactory();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		// replace tier description
		for(int i = 0; i < session.getUserTierCount(); i++) {
			final TierDescription td = session.getUserTier(i);
			
			if(td.getName().equals(tierName)) {
				final TierDescription newDesc = factory.createTierDescription(newTierName, td.isGrouped());
				session.removeUserTier(td);
				session.addUserTier(i, newDesc);
			}
		}
		
		// fix name in tier view
		final List<TierViewItem> oldTierView = session.getTierView();
		final List<TierViewItem> newTierView = new ArrayList<TierViewItem>();
		for(TierViewItem tv:oldTierView) {
			if(tv.getTierName().equals(tierName)) {
				final TierViewItem newItem = 
						factory.createTierViewItem(newTierName, tv.isVisible(), tv.getTierFont(), tv.isTierLocked());
				newTierView.add(newItem);
			} else {
				newTierView.add(tv);
			}
		}
		session.setTierView(newTierView);
		
		// change tier name in records
		for(Record r:session.getRecords()) {
			if(r.hasTier(tierName)) {
				final Tier<String> oldTier = r.getTier(tierName, String.class);
				r.removeTier(tierName);
				
				final Tier<String> newTier = factory.createTier(newTierName);
				for(String v:oldTier) 
					newTier.addGroup(v);
				r.putTier(newTier);
			}
		}
		
		super.queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newTierView);
	}
}

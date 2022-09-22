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
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Add a new tier to the session.  This will also add a new empty tier
 * to each record.
 *
 */
public class AddTierEdit extends SessionEditorUndoableEdit {

	private final TierDescription tierDescription;
	
	private final TierViewItem tierViewItem;

	private int index = -1;

	public AddTierEdit(SessionEditor editor, TierDescription tierDesc, TierViewItem tvi) {
		this(editor, tierDesc, tvi, -1);
	}


	public AddTierEdit(SessionEditor editor, TierDescription tierDesc, TierViewItem tvi, int index) {
		super(editor);
		this.tierDescription = tierDesc;
		this.tierViewItem = tvi;
		this.index = index;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo add tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo add tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.removeUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		newView.remove(this.tierViewItem);
		session.setTierView(newView);
		
		for(Record r:session.getRecords()) {
			r.removeTier(tierDescription.getName());
		}

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(tierView, newView));
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.addUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		if(this.index >= 0)
			newView.add(index, this.tierViewItem);
		else
			newView.add(this.tierViewItem);
		session.setTierView(newView);
		
		SessionFactory factory = SessionFactory.newFactory();
		for(Record r:session.getRecords()) {
			if(!r.hasTier(tierViewItem.getTierName())) {
				Tier<?> tier = factory.createTier(tierDescription.getName(), tierDescription.getDeclaredType(), tierDescription.isGrouped());
				if(tierDescription.isGrouped()) {
					for (int gIdx = 0; gIdx < r.numberOfGroups(); gIdx++) {
						tier.addGroup();
					}
				} else {
					tier.addGroup();
				}
				r.putTier(tier);
			}
		}

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(tierView, newView));
		getEditor().getEventManager().queueEvent(ee);
	}

}

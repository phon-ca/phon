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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.TierViewItem;

public class ToggleLockAllTiersAction extends TierManagementAction {

	private static final long serialVersionUID = -4955905767921862857L;
	
	public ToggleLockAllTiersAction(SessionEditor editor,
			TierOrderingEditorView view) {
		super(editor, view);
		
		final boolean locked = areAllLocked();
		putValue(NAME, (locked ? "Unlock " : "Lock ") + "all tiers");
	}
	
	private boolean areAllLocked() {
		boolean retVal = true;
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final List<TierViewItem> view = session.getTierView();
		for(TierViewItem tvi:view) {
			retVal &= tvi.isTierLocked();
		}
		
		return retVal;
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final List<TierViewItem> view = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>();
		final SessionFactory factory = SessionFactory.newFactory();
		
		final boolean locked = !areAllLocked();
		for(TierViewItem oldItem:view) {
			final TierViewItem newItem = factory.createTierViewItem(oldItem.getTierName(), oldItem.isVisible(),
					oldItem.getTierFont(), locked);
			newView.add(newItem);
		}
		
		final TierViewEdit edit = new TierViewEdit(getEditor(), view, newView);
		editor.getUndoSupport().postEdit(edit);
		
		putValue(NAME, (locked ? "Unlock " : "Lock ") + "all tiers");
	}

}

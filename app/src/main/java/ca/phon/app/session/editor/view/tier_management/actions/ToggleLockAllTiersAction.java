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
package ca.phon.app.session.editor.view.tier_management.actions;

import java.awt.event.ActionEvent;
import java.util.*;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.*;

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

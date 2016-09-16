/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.util.ArrayList;
import java.util.List;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.TierViewItem;

public class MoveTierAction extends TierManagementAction {
	
	private static final long serialVersionUID = 4932691213871636212L;
	
	private final TierViewItem item;

	private final int direction;
		
	public MoveTierAction(SessionEditor editor, TierOrderingEditorView view,
			TierViewItem item, int direction) {
		super(editor, view);
		this.item = item;
		this.direction = direction;
		
		putValue(NAME, "Move tier " + (direction < 0 ? "up" : "down"));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final List<TierViewItem> view = getEditor().getSession().getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(view);
		final int currentIndex = view.indexOf(item);
		final int nextIndex = currentIndex + direction;
		if(nextIndex >= 0 && nextIndex < view.size()) {
			newView.remove(currentIndex);
			newView.add(nextIndex, item);
			
			final TierViewEdit edit = new TierViewEdit(getEditor(), view, newView);
			getEditor().getUndoSupport().postEdit(edit);
			
			getView().getTierOrderingTable().getSelectionModel().setSelectionInterval(nextIndex, nextIndex);
		}
	}

}

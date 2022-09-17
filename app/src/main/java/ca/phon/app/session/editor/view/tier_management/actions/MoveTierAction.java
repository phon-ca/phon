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
package ca.phon.app.session.editor.view.tier_management.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.TierViewItem;

import java.awt.event.ActionEvent;
import java.util.*;

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

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
package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.session.*;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class ToggleHideAllTiersAction extends SessionEditorAction {

	public ToggleHideAllTiersAction(SessionEditor editor) {
		super(editor);
		
		final boolean allVisible = areAllVisible();
		putValue(NAME, (allVisible ? "Hide " : "Show ") + "all tiers");
		putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(allVisible ? "Visibility_Off" : "visibility", IconSize.SMALL, UIManager.getColor("Button.foreground")));
	}
	
	private boolean areAllVisible() {
		boolean retVal = true;
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final List<TierViewItem> view = session.getTierView();
		for(TierViewItem tvi:view) {
			retVal &= tvi.isVisible();
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
		
		final boolean allVisible = areAllVisible();
		for(TierViewItem oldItem:view) {
			final TierViewItem newItem = factory.createTierViewItem(oldItem.getTierName(), !allVisible,
					oldItem.getTierFont(), oldItem.isTierLocked());
			newView.add(newItem);
		}
		
		final TierViewEdit edit = new TierViewEdit(getEditor(), view, newView);
		editor.getUndoSupport().postEdit(edit);
		
		putValue(NAME, (!allVisible ? "Hide " : "Show ") + "all tiers");
	}

}

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

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.MoveTierEdit;
import ca.phon.app.session.editor.undo.SessionEditUndoSupport;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class MoveTierAction extends SessionEditorAction {

	private final TierViewItem item;

	private final int direction;
		
	public MoveTierAction(SessionEditor editor, TierViewItem item, int direction) {
		super(editor);
		this.item = item;
		this.direction = direction;

		putValue(NAME, "Move tier " + (direction < 0 ? "up" : "down"));
		putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(direction < 0 ? "arrow_upward" : "arrow_downward", IconSize.SMALL, UIManager.getColor("Button.foreground")));
	}

	public MoveTierAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, TierViewItem item, int direction) {
		super(session, eventManager, undoSupport);
		this.item = item;
		this.direction = direction;
		
		putValue(NAME, "Move tier " + (direction < 0 ? "up" : "down"));
		putValue(SMALL_ICON, IconManager.getInstance().getFontIcon(direction < 0 ? "arrow_upward" : "arrow_downward", IconSize.SMALL, UIManager.getColor("Button.foreground")));
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final List<TierViewItem> view = getSession().getTierView();
		final List<TierViewItem> newView = new ArrayList<>(view);
		final int currentIndex = newView.indexOf(item);
		final MoveTierEdit edit = new MoveTierEdit(getSession(), getEventManager(), item, currentIndex + direction);
		getUndoSupport().postEdit(edit);
	}

}

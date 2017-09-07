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
package ca.phon.app.session.editor.undo;

import java.util.*;

import ca.phon.app.session.editor.*;
import ca.phon.session.TierViewItem;

public class TierViewItemEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 5225502635375340813L;
	
	private final TierViewItem oldItem;
	
	private final TierViewItem newItem;
	
	public TierViewItemEdit(SessionEditor editor, TierViewItem oldItem, TierViewItem newItem) {
		super(editor);
		this.oldItem = oldItem;
		this.newItem = newItem;
	}
	
	@Override
	public void undo() {
		final List<TierViewItem> tierView = new ArrayList<TierViewItem>(getEditor().getSession().getTierView());
		final int idx = tierView.indexOf(newItem);
		tierView.remove(idx);
		tierView.add(idx, oldItem);
		
		getEditor().getSession().setTierView(tierView);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getEditor().getUndoSupport(), newItem);
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final List<TierViewItem> tierView = new ArrayList<TierViewItem>(getEditor().getSession().getTierView());
		final int idx = tierView.indexOf(oldItem);
		tierView.remove(idx);
		tierView.add(idx, newItem);
		
		getEditor().getSession().setTierView(tierView);
		
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newItem);
		getEditor().getEventManager().queueEvent(ee);
	}

}

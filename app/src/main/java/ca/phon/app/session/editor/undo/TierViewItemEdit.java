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

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
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

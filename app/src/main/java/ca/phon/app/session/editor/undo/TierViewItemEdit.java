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
import ca.phon.session.TierViewItem;

import java.util.*;

public class TierViewItemEdit extends SessionEditorUndoableEdit {

	private final TierViewItem oldItem;
	
	private final TierViewItem newItem;
	
	public TierViewItemEdit(SessionEditor editor, TierViewItem oldItem, TierViewItem newItem) {
		super(editor);
		this.oldItem = oldItem;
		this.newItem = newItem;
	}
	
	@Override
	public void undo() {
		final List<TierViewItem> oldView = getEditor().getSession().getTierView();
		final List<TierViewItem> tierView = new ArrayList<>(oldView);
		final int idx = tierView.indexOf(newItem);
		tierView.remove(idx);
		tierView.add(idx, oldItem);
		
		getEditor().getSession().setTierView(tierView);
		
		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getEditor(), new EditorEventType.TierViewChangedData(oldView, tierView));
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final List<TierViewItem> oldView = getEditor().getSession().getTierView();
		final List<TierViewItem> tierView = new ArrayList<>(oldView);
		final int idx = tierView.indexOf(oldItem);
		tierView.remove(idx);
		tierView.add(idx, newItem);
		
		getEditor().getSession().setTierView(tierView);

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getEditor(), new EditorEventType.TierViewChangedData(oldView, tierView));
		getEditor().getEventManager().queueEvent(ee);
	}

}

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
import ca.phon.session.*;

import javax.swing.undo.CannotUndoException;
import java.util.*;

/**
 * Changes to the tier view including order, visibility and locking.
 *
 */
public class TierViewEdit extends SessionUndoableEdit {

	/**
	 * Old view
	 */
	private final List<TierViewItem> oldView;
	
	/**
	 * New view
	 */
	private final List<TierViewItem> newView;

	public TierViewEdit(SessionEditor editor, List<TierViewItem> oldView, List<TierViewItem> newView) {
		this(editor.getSession(), editor.getEventManager(), oldView, newView);
	}

	public TierViewEdit(Session session, EditorEventManager editorEventManager, List<TierViewItem> oldView, List<TierViewItem> newView) {
		super(session, editorEventManager);
		this.oldView = new ArrayList<>(oldView);
		this.newView = newView;
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo set tier order";
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo set tier order";
	}
	
	@Override
	public void undo() throws CannotUndoException {
		final Session session = getSession();
		session.setTierView(oldView);

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(newView, oldView, EditorEventType.TierViewChangeType.RELOAD, new ArrayList<>(), new ArrayList<>()));
		getEditorEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final Session session = getSession();
		session.setTierView(newView);

		final EditorEvent<EditorEventType.TierViewChangedData> ee =
				new EditorEvent<>(EditorEventType.TierViewChanged, getSource(), new EditorEventType.TierViewChangedData(oldView, newView, EditorEventType.TierViewChangeType.RELOAD, new ArrayList<>(), new ArrayList<>()));
		getEditorEventManager().queueEvent(ee);
	}
	
}

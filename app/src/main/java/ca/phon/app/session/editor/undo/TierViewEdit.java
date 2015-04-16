/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Session;
import ca.phon.session.TierViewItem;

/**
 * Changes to the tier view including order, visibility and locking.
 *
 */
public class TierViewEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 8337753863840703579L;

	/**
	 * Old view
	 */
	private final List<TierViewItem> oldView;
	
	/**
	 * New view
	 */
	private final List<TierViewItem> newView;
	
	public TierViewEdit(SessionEditor editor, List<TierViewItem> oldView, List<TierViewItem> newView) {
		super(editor);
		this.oldView = new ArrayList<TierViewItem>(oldView);
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
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setTierView(oldView);
		
		super.queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, editor.getUndoSupport(), oldView);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		session.setTierView(newView);
		
		super.queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
	}
	
}

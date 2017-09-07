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

import javax.swing.undo.CannotUndoException;

import ca.phon.app.session.editor.*;
import ca.phon.session.*;

public class AddTierEdit extends SessionEditorUndoableEdit {

	private static final long serialVersionUID = 5600095287675463984L;

	protected final TierDescription tierDescription;
	
	protected final TierViewItem tierViewItem;
	
	public AddTierEdit(SessionEditor editor, TierDescription tierDesc, TierViewItem tvi) {
		super(editor);
		this.tierDescription = tierDesc;
		this.tierViewItem = tvi;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo add tier " + tierDescription.getName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo add tier " + tierDescription.getName();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		final Object oldSource = getSource();
		setSource(editor.getUndoSupport());
		
		session.removeUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		newView.remove(this.tierViewItem);
		session.setTierView(newView);
		
		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
		
		setSource(oldSource);
	}

	@Override
	public void doIt() {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		session.addUserTier(tierDescription);
		
		final List<TierViewItem> tierView = session.getTierView();
		final List<TierViewItem> newView = new ArrayList<TierViewItem>(tierView);
		newView.add(this.tierViewItem);
		session.setTierView(newView);
		
		queueEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, getSource(), newView);
	}

}

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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Tier;

/**
 * A change to the value of a group in a tier.
 * 
 */
public class TierEdit<T> extends SessionEditorUndoableEdit {
	
	private static final long serialVersionUID = -3236844601334798650L;

	/**
	 * tier
	 */
	private final Tier<T> tier;
	
	/**
	 * Group
	 */
	private final int groupIndex;
	
	/**
	 * Old value
	 */
	private T oldValue;
	
	/**
	 * New value
	 */
	private final T newValue;
	
	/**
	 * Tells this edit to fire a 'hard' change on undo.
	 * A 'hard' change calls TIER_CHANGED_EVENT after TIER_CHANGE_EVENT
	 */
	private boolean fireHardChangeOnUndo = false;
	
	/**
	 * Constructor 
	 * 
	 * @param group
	 * @param tierName
	 * @param oldValue
	 * @param newValue
	 */
	public TierEdit(SessionEditor editor, Tier<T> tier, int groupIndex, T newValue) {
		super(editor);
		this.tier = tier;
		this.groupIndex = groupIndex;
		this.newValue = newValue;
	}
	
	@Override
	public String getUndoPresentationName() {
		return "Undo edit tier " + tier.getName();
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo edit tier " + tier.getName();
	}

	public T getOldValue() {
		return oldValue;
	}

	public void setOldValue(T oldValue) {
		this.oldValue = oldValue;
	}

	public Tier<T> getTier() {
		return tier;
	}

	public int getGroupIndex() {
		return groupIndex;
	}
	
	public T getNewValue() {
		return newValue;
	}
	
	public boolean isFireHardChangeOnUndo() {
		return fireHardChangeOnUndo;
	}

	public void setFireHardChangeOnUndo(boolean fireHardChangeOnUndo) {
		this.fireHardChangeOnUndo = fireHardChangeOnUndo;
	}
	
	@Override
	public void redo() {
		super.redo();
		
		if(getEditor() != null) {
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getEditor().getUndoSupport(), tier.getName());
			if(isFireHardChangeOnUndo()) {
				queueEvent(EditorEventType.TIER_CHANGED_EVT, getEditor().getUndoSupport(), tier.getName());
			}
		}
	}

	@Override
	public void undo() {
		super.undo();

		final T oldVal = getOldValue();
		tier.setGroup(groupIndex, oldVal);
		
		if(getEditor() != null) {
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getEditor().getUndoSupport(), tier.getName());
			if(isFireHardChangeOnUndo()) {
				queueEvent(EditorEventType.TIER_CHANGED_EVT, getEditor().getUndoSupport(), tier.getName());
			}
		}
	}
	
	@Override
	public void doIt() {
		if(groupIndex < tier.numberOfGroups()) {
			setOldValue(tier.getGroup(groupIndex));			
			tier.setGroup(groupIndex, newValue);
		} else {
			while(tier.numberOfGroups() < groupIndex) tier.addGroup();
			tier.addGroup(newValue);
		}
		
		if(getEditor() != null)
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getSource(), tier.getName());
	}

}

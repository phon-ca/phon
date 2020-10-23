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

import ca.phon.app.session.editor.*;
import ca.phon.session.*;

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
		
		if(getEditor() != null) { 
			queueEvent(EditorEventType.TIER_CHANGE_EVT, getSource(), tier.getName());
			if(isFireHardChangeOnUndo()) {
				queueEvent(EditorEventType.TIER_CHANGED_EVT, getSource(), tier.getName());
			}
		}
	}

}

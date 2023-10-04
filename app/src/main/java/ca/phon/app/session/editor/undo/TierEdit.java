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
import ca.phon.extensions.IExtendable;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.session.Session;
import ca.phon.session.Tier;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * A change to the value of a group in a tier.
 * 
 */
public class TierEdit<T> extends SessionUndoableEdit {
	
	/**
	 * tier
	 */
	private final Tier<T> tier;
	
	/**
	 * Old value
	 */
	private T oldValue;
	
	/**
	 * New value
	 */
	private T newValue;
	
	/**
	 * Tells this edit to fire a 'hard' change on undo.
	 * A 'hard' change calls TIER_CHANGED_EVENT after TIER_CHANGE_EVENT
	 */
	private boolean fireHardChangeOnUndo = false;

	public TierEdit(SessionEditor editor, Tier<T> tier, T newValue) {
		this(editor.getSession(), editor.getEventManager(), tier, newValue);
	}

	/**
	 * Constructor 
	 * 
	 * @param session
	 * @param editorEventManager
	 * @param tier
	 * @param newValue
	 */
	public TierEdit(Session session, EditorEventManager editorEventManager, Tier<T> tier, T newValue) {
		super(session, editorEventManager);
		this.tier = tier;
		this.newValue = newValue;
	}

	public TierEdit(SessionEditor editor, Tier<T> tier, String text) {
		this(editor.getSession(), editor.getEventManager(), tier, text);
	}

	public TierEdit(Session session, EditorEventManager editorEventManager, Tier<T> tier, String text) {
		super(session, editorEventManager);
		this.tier = tier;

		final Formatter<T> formatter = FormatterFactory.createFormatter(tier.getDeclaredType());
		try {
			final T parsedValue = formatter.parse(text);
			this.newValue = parsedValue;
		} catch (ParseException pe) {
			// attempt to create a new instance of the object
			try {
				final T val = tier.getDeclaredType().getDeclaredConstructor().newInstance();
				if(val instanceof IExtendable) {
					((IExtendable) val).putExtension(UnvalidatedValue.class, new UnvalidatedValue(text, pe));
				}
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {}
		}
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
	public void undo() {
		super.undo();

		final T oldVal = getOldValue();
		tier.setValue(oldVal);
		
		if(getEditorEventManager() != null) {
			final EditorEventType.TierChangeData tcd = new EditorEventType.TierChangeData(tier, newValue, oldVal);
			final EditorEvent<EditorEventType.TierChangeData> tierChangeEvt =
					new EditorEvent<>(EditorEventType.TierChange, getSource(), tcd);
			getEditorEventManager().queueEvent(tierChangeEvt);
			if(isFireHardChangeOnUndo()) {
				final EditorEvent<EditorEventType.TierChangeData> tierChangedEvt =
						new EditorEvent<>(EditorEventType.TierChanged, getSource(), tcd);
				getEditorEventManager().queueEvent(tierChangedEvt);
			}
		}
	}
	
	@Override
	public void doIt() {
		Tier<T> tier = getTier();
		T newValue = getNewValue();
		tier.setValue(newValue);

		if(getEditorEventManager() != null) {
			final EditorEventType.TierChangeData tcd = new EditorEventType.TierChangeData(tier, getOldValue(), newValue);
			final EditorEvent<EditorEventType.TierChangeData> tierChangeEvt =
					new EditorEvent<>(EditorEventType.TierChange, getSource(), tcd);
			getEditorEventManager().queueEvent(tierChangeEvt);
			if(isFireHardChangeOnUndo()) {
				final EditorEvent<EditorEventType.TierChangeData> tierChangedEvt =
						new EditorEvent<>(EditorEventType.TierChanged, getSource(), tcd);
				getEditorEventManager().queueEvent(tierChangedEvt);
			}
		}
	}

}

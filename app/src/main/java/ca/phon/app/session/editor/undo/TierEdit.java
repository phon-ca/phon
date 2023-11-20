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
import ca.phon.extensions.Extension;
import ca.phon.extensions.IExtendable;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllabifier.Syllabifier;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>A change to the value of a {@link Tier}. The record parameter is optional, however if it is not
 * null additional operations (such as syllabification and alignment) will be performed on
 * dependent tiers. If the tier is not currently in the record it is added when this edit is performed.</p>
 *
 * <p>An {@link EditorEventType#TierChange} event will be issued for each altered tier with valueAdjusting set to true.
 * If this edit's valueAdjusting is false, another event will also be issued for each tier with valueAdjusting set to false.
 * This should be the end of a sequence of change events on the same tier.  By default valueAdjusting is true.</p>
 *
 * <p>If the tier is blind and the provided transcriber is not {@link Transcriber#VALIDATOR} Blind transcriptions are
 * modified instead of tier value. When modifying blind transcriptions {@link ca.phon.app.session.editor.EditorEventType.TierChangeData#oldValue()}
 * and {@link ca.phon.app.session.editor.EditorEventType.TierChangeData#newValue()} will be the values for the provided
 * transcriber and <em>not</em> the value returned by {@link Tier#getValue()}.</p>
 *
 * <p>Secondary behaviours may be added to tier edits by supplying an implementation of {@link DependentTierChanges} as
 * an extension on the tier object. Secondary actions are responsible for firing TierChange events for any modified tiers.
 * Multiple secondary actions may be added using {@link DependentTierChangeChain}.</p>
 */
public class TierEdit<T> extends SessionUndoableEdit {

	/**
	 * transcriber (default: Transcriber.VALIDATOR)
	 */
	private final Transcriber transcriber;

	/**
	 * record
	 */
	private final Record record;

	/**
	 * tier
	 */
	private final Tier<T> tier;

	/**
	 * did we add the tier to the record
	 */
	private boolean tierAddedToRecord = false;
	
	/**
	 * Old value
	 */
	private final T oldValue;
	
	/**
	 * New value
	 */
	private final T newValue;

	/**
	 * Map of dependent tier changes
	 */
	private final Map<String, Object> additionalTierChanges = new LinkedHashMap<>();

	/**
	 * Is this event one of many in a sequence (default: false)
	 *
	 */
	private boolean valueAdjusting = false;

	public TierEdit(SessionEditor editor, Tier<T> tier, T newValue) {
		this(editor, editor.getDataModel().getTranscriber(), editor.currentRecord(), tier, newValue);
	}

	/**
	 * New tier editor event with transcriber specified
	 * 
	 * @param editor
	 * @param transcriber
	 * @param record
	 * @param tier
	 * @param newValue
	 */
	public TierEdit(SessionEditor editor, Transcriber transcriber, Record record, Tier<T> tier, T newValue) {
		this(editor.getSession(), editor.getEventManager(), transcriber, record, tier, newValue);
	}

	/**
	 * With record provided if not the current record
	 *
	 * @param editor
	 * @param record
	 * @param tier
	 * @param newValue
	 */
	public TierEdit(SessionEditor editor, Record record, Tier<T> tier, T newValue) {
		this(editor.getSession(), editor.getEventManager(), record, tier, newValue);
	}

	public TierEdit(Session session, EditorEventManager editorEventManager, Record record, Tier<T> tier, T newValue) {
		this(session, editorEventManager, Transcriber.VALIDATOR, record, tier, newValue);
	}

	public TierEdit(Session session, EditorEventManager editorEventManager, Transcriber transcriber, Record record, Tier<T> tier, T newValue) {
		this(session, editorEventManager, transcriber, record, tier, newValue, true);
	}

	/**
	 * Tier edit with all parameters specified
	 *
	 * @param session
	 * @param editorEventManager
	 * @param transcriber
	 * @param record
	 * @param tier
	 * @param newValue
	 * @param valueAdjusting
	 */
	public TierEdit(Session session, EditorEventManager editorEventManager, Transcriber transcriber, Record record, Tier<T> tier, T newValue, boolean valueAdjusting) {
		super(session, editorEventManager);
		this.tier = tier;
		this.transcriber = transcriber;
		this.record = record;
		this.newValue = newValue;
		this.oldValue = tier.getValue();
		this.valueAdjusting = valueAdjusting;
	}

	public TierEdit(SessionEditor editor, Tier<T> tier, String text) {
		this(editor, editor.currentRecord(), tier, text);
	}

	public TierEdit(SessionEditor editor, Record record, Tier<T> tier, String text) {
		this(editor.getSession(), editor.getEventManager(), record, tier, text);
	}

	/**
	 * Tier edit using text as new tier value
	 *
	 * @param session
	 * @param editorEventManager
	 * @param record
	 * @param tier
	 * @param text
	 */
	public TierEdit(Session session, EditorEventManager editorEventManager, Record record, Tier<T> tier, String text) {
		this(session, editorEventManager, Transcriber.VALIDATOR, record, tier, text);
	}

	public TierEdit(Session session, EditorEventManager editorEventManager, Transcriber transcriber, @Nullable Record record, Tier<T> tier, String text) {
		this(session, editorEventManager, transcriber, record, tier, text, true);
	}

	/**
	 * Tier edit using text with all parameters specified
	 *
	 * @param session
	 * @param editorEventManager
	 * @param transcriber
	 * @param record if not provided secondary tier actions are not performed
	 * @param tier
	 * @param text
	 * @param valueAdjusting
	 */
	public TierEdit(Session session, EditorEventManager editorEventManager, Transcriber transcriber, @Nullable Record record, Tier<T> tier, String text, boolean valueAdjusting) {
		super(session, editorEventManager);
		T nv;
		this.transcriber = transcriber;
		this.tier = tier;
		this.record = record;
		this.oldValue = tier.getValue();
		this.valueAdjusting = valueAdjusting;

		final Formatter<T> formatter = FormatterFactory.createFormatter(tier.getDeclaredType());
		try {
			final T parsedValue = formatter.parse(text);
			nv = parsedValue;
		} catch (ParseException pe) {
			// attempt to create a new instance of the object
			try {
				final T val = tier.getDeclaredType().getDeclaredConstructor().newInstance();
				if(val instanceof IExtendable) {
					((IExtendable) val).putExtension(UnvalidatedValue.class, new UnvalidatedValue(text, pe));
				}
				nv = val;
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				nv = null;
			}
		}
		this.newValue = nv;
	}
	
	@Override
	public String getUndoPresentationName() {
		return "Undo edit tier " + tier.getName();
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo edit tier " + tier.getName();
	}

	public Transcriber getTranscriber() {
		if(tier.isBlind())
			return transcriber;
		else
			return Transcriber.VALIDATOR;
	}

	public Record getRecord() {
		return record;
	}

	public T getOldValue() {
		return oldValue;
	}

	public Tier<T> getTier() {
		return tier;
	}

	public T getNewValue() {
		return newValue;
	}

	public boolean isValueAdjusting() {
		return valueAdjusting;
	}

	public void setValueAdjusting(boolean valueAdjusting) {
		this.valueAdjusting = valueAdjusting;
	}

	public void putAdditionalTierChange(String tierName, Object value) {
		additionalTierChanges.put(tierName, value);
	}

	public Object getAdditionalTierChange(String tierName) {
		return additionalTierChanges.get(tierName);
	}

	/**
	 * @return !isValueIsAdjusting()z
	 * @deprecated opposite of valueAdjusting
	 */
	@Deprecated
	public boolean isFireHardChangeOnUndo() {
		return !valueAdjusting;
	}

	/**
	 * @param fireHardChangeOnUndo
	 * @deprecated use setValueAdjusting
	 */
	@Deprecated
	public void setFireHardChangeOnUndo(boolean fireHardChangeOnUndo) {
		this.valueAdjusting = !fireHardChangeOnUndo;
	}

	/**
	 * Fire tier change events for given tier oldLoc value and new value
	 *
	 * @param tier
	 * @param oldValue
	 * @param newValue
	 * @param <R> type of provided tier and values
	 */
	public <R> void fireTierChange(Tier<R> tier, R oldValue, R newValue) {
		if(getEditorEventManager() == null) return;
		final EditorEventType.TierChangeData tcd = new EditorEventType.TierChangeData(
				tier.isBlind() ? getTranscriber() : Transcriber.VALIDATOR, getRecord(), tier, oldValue, newValue, true);
		final EditorEvent<EditorEventType.TierChangeData> tierChangeEvt = new EditorEvent<>(EditorEventType.TierChange, getSource(), tcd);
		getEditorEventManager().queueEvent(tierChangeEvt);
		if(!isValueAdjusting()) {
			final EditorEventType.TierChangeData tcdEnd = new EditorEventType.TierChangeData(getTranscriber(), getRecord(), tier, oldValue, newValue, false);
			final EditorEvent<EditorEventType.TierChangeData> tierChangedEvt = new EditorEvent<>(EditorEventType.TierChange, getSource(), tcdEnd);
			getEditorEventManager().queueEvent(tierChangedEvt);
		}
	}

	@Override
	public void doIt() {
		Tier<T> tier = getTier();

		T newValue = getNewValue();
		if(tier.getDeclaredType() == IPATranscript.class && !((IPATranscript)newValue).hasSyllableInformation()) {
			final IPATranscript ipa = (IPATranscript) newValue;
			@SuppressWarnings("unchecked")
			final Syllabifier syllabifier = SyllabifierOptions.findSyllabifier(getSession(), getRecord(), (Tier<IPATranscript>) tier);
			if (syllabifier != null) {
				syllabifier.syllabify(ipa.toList());
				// will apply additional annotations
				ipa.syllables();
			}
		}

		if(getTranscriber() == Transcriber.VALIDATOR) {
			tier.setValue(newValue);
		} else {
			tier.setBlindTranscription(getTranscriber().getUsername(), newValue);
		}

		if (this.record != null) {
			if(this.record.getTier(tier.getName()) != tier) {
				this.record.putTier(tier);
				tierAddedToRecord = true;
			}
		}

		fireTierChange(tier, getOldValue(), newValue);
		if(record != null)
			performDependentTierChanges();
	}

	@Override
	public void undo() {
		super.undo();

		final T oldVal = getOldValue();
		tier.setValue(oldVal);

		if(this.record != null && tierAddedToRecord) {
			this.record.removeTier(getTier().getName());
		}
		
		fireTierChange(tier, newValue, oldVal);
		if(record != null)
			performDependentTierChanges();
	}

	/**
	 * Called on doIt() and undo() this will apply any dependent tier changes such as
	 * phone alignment or segment adjustments.
	 */
	protected void performDependentTierChanges() {
		// perform any other tier changes specified by tier
		@SuppressWarnings("unchecked")
		final DependentTierChanges<T> otherTierChanges = tier.getExtension(DependentTierChanges.class);
		if(otherTierChanges != null) {
			otherTierChanges.performDependentTierChanges(this);
		}
	}

	/**
	 * Extension for tiers which allow for automatic changes to dependent tiers to be setup at runtime.
	 * These changes will be performed after all default dependent tier changes.
	 *
	 * @param <T> must match tier
	 */
	@Extension(Tier.class)
	@FunctionalInterface
	public static interface DependentTierChanges<T> {
		/**
		 * Perform dependent tier changes, should throw a runtime exception (up to implementation) if
		 * given TierEdit parameterized type does not match our parameterized type.
		 *
		 * @param tierEdit
		 *
		 * @throws RuntimeException usually {@link IllegalStateException} on type mismatch
		 */
		void performDependentTierChanges(TierEdit<T> tierEdit);
	}

	/**
	 * Chain dependent tier changes.  Used if existing dependent tier changes are found for a tier.
	 *
	 *  E.g.,
	 * <pre>
	 *     DependentTierChanges myChanges = ...;
	 *     DependentTierChanges otherChanges = tier.getExtension(DependentTierChanges.class);
	 *     if(otherChanges != null) {
	 *         // order given is order performed
	 *         tier.putExtension(DependentTierChanges.class, new DependentTierChangeChain(myChanges, otherChanges);
	 *     } else {
	 *         tier.putExtension(DependentTierChanges.class, myChanges);
	 *     }
	 * </pre>
	 * @param <T> must match tier
	 */
	public static class DependentTierChangeChain<T> implements DependentTierChanges<T> {
		private final DependentTierChanges<T> otherChanges;

		private final DependentTierChanges<T> changes;

		public DependentTierChangeChain(DependentTierChanges<T> changes, DependentTierChanges<T> otherChanges) {
			this.changes = changes;
			this.otherChanges = otherChanges;
		}

		@Override
		public void performDependentTierChanges(TierEdit<T> tierEdit) {
			if(this.changes != null) {
				this.changes.performDependentTierChanges(tierEdit);
			}
			if(otherChanges != null) {
				otherChanges.performDependentTierChanges(tierEdit);
			}
		}

	}

}

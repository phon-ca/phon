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

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.*;
import ca.phon.extensions.IExtendable;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Language;

import javax.swing.undo.CompoundEdit;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A change to the value of a group in a tier.
 * 
 */
public class TierEdit<T> extends SessionUndoableEdit {

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
	private boolean addedRecord = false;
	
	/**
	 * Old value
	 */
	private T oldValue;
	
	/**
	 * New value
	 */
	private T newValue;

	/**
	 * Map of dependent tier changes
	 */
	private final Map<String, Object> additionalTierChanges = new LinkedHashMap<>();
	
	/**
	 * Tells this edit to fire a 'hard' change on undo.
	 * A 'hard' change calls TIER_CHANGED_EVENT after TIER_CHANGE_EVENT
	 */
	private boolean fireHardChangeOnUndo = false;

	public TierEdit(SessionEditor editor, Tier<T> tier, T newValue) {
		this(editor, editor.currentRecord(), tier, newValue);
	}

	public TierEdit(SessionEditor editor, Record record, Tier<T> tier, T newValue) {
		this(editor.getSession(), editor.getEventManager(), record, tier, newValue);
	}

	/**
	 * Constructor 
	 * 
	 * @param session
	 * @param editorEventManager
	 * @param tier
	 * @param newValue
	 */
	public TierEdit(Session session, EditorEventManager editorEventManager, Record record, Tier<T> tier, T newValue) {
		super(session, editorEventManager);
		this.tier = tier;
		this.record = record;
		this.newValue = newValue;
	}

	public TierEdit(SessionEditor editor, Tier<T> tier, String text) {
		this(editor, editor.currentRecord(), tier, text);
	}

	public TierEdit(SessionEditor editor, Record record, Tier<T> tier, String text) {
		this(editor.getSession(), editor.getEventManager(), record, tier, text);
	}

	public TierEdit(Session session, EditorEventManager editorEventManager, Record record, Tier<T> tier, String text) {
		super(session, editorEventManager);
		this.tier = tier;
		this.record = record;

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

		if(this.record != null && addedRecord) {
			this.record.removeTier(getTier().getName());
		}
		
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

		if(record != null)
			performAdditionalTierChanges();
	}

	private Syllabifier getSyllabifier(Tier<IPATranscript> tier) {
		Syllabifier retVal = null;
		final Session session = getSession();
		// new method
		// TODO move this key somewhere sensible, currently unused
		if(tier.getTierParameters().containsKey("syllabifier")) {
			try {
				final Language lang = Language.parseLanguage(tier.getTierParameters().get("syllabifier"));
				if(lang != null && SyllabifierLibrary.getInstance().availableSyllabifierLanguages().contains(lang)) {
					retVal = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(lang);
				}
			} catch (IllegalArgumentException e) {
				LogUtil.warning(e);
			}
		}
		if(retVal == null) {
			// old method
			final SyllabifierInfo info = session.getExtension(SyllabifierInfo.class);
			if (info != null) {
				final Language lang = info.getSyllabifierLanguageForTier(tier.getName());
				if (lang != null && SyllabifierLibrary.getInstance().availableSyllabifierLanguages().contains(lang)) {
					retVal = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(lang);
				}
			}
		}
		if(retVal == null) {
			retVal = SyllabifierLibrary.getInstance().defaultSyllabifier();
		}
		return retVal;
	}

	/**
	 * Called on doIt() and undo() this will apply any dependent tier changes such as
	 * phone alignment or segment adjustments.
	 */
	protected void performAdditionalTierChanges() {
		// if tier is IPATarget or IPAActual update default phone alignment
		// TODO update potential user-defined tier alignments
		final SystemTierType systemTierType = SystemTierType.tierFromString(tier.getName());
		if(systemTierType == SystemTierType.IPATarget || systemTierType == SystemTierType.IPAActual) {
			PhoneAlignment pm = null;
			if(additionalTierChanges.containsKey(SystemTierType.PhoneAlignment.getName())) {
				pm = (PhoneAlignment) additionalTierChanges.get(SystemTierType.PhoneAlignment.getName());
			} else {
				// update alignment
				pm = PhoneAlignment.fromTiers(record.getIPATargetTier(), record.getIPAActualTier());
			}
			if(pm != null) {
				final PhoneAlignment oldVal = record.getPhoneAlignment();
				record.setPhoneAlignment(pm);
				additionalTierChanges.put(SystemTierType.PhoneAlignment.getName(), oldVal);

				// fire event for phone alignment tier change
				final EditorEventType<EditorEventType.TierChangeData> tierChangedEvent =
						isFireHardChangeOnUndo() ? EditorEventType.TierChanged : EditorEventType.TierChange;
				final EditorEvent<EditorEventType.TierChangeData> pmEvent = new EditorEvent<>(tierChangedEvent,
						CommonModuleFrame.getCurrentFrame(), new EditorEventType.TierChangeData(tier, oldVal, pm));
				getEditorEventManager().queueEvent(pmEvent);
			}
		}
	}
	
	@Override
	public void doIt() {
		Tier<T> tier = getTier();
		T newValue = getNewValue();
		tier.setValue(newValue);

		if (this.record != null) {
			if(this.record.getTier(tier.getName()) != tier) {
				this.record.putTier(tier);
				 addedRecord = true;
			}
		}

		if(tier.getDeclaredType() == IPATranscript.class && !((IPATranscript)tier.getValue()).hasSyllableInformation()) {
			final IPATranscript ipa = (IPATranscript) tier.getValue();
			@SuppressWarnings("unchecked")
			final Syllabifier syllabifier = getSyllabifier((Tier<IPATranscript>) tier);
			if (syllabifier != null) {
				syllabifier.syllabify(ipa.toList());
				// will apply additional annotations
				ipa.syllables();
			}
		}

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

		if(record != null)
			performAdditionalTierChanges();
	}

}

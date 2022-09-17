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
package ca.phon.app.session.editor.view.common;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.record_data.IPAFieldTooltip;
import ca.phon.app.session.editor.view.syllabification_and_alignment.*;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.ipa.*;

import java.beans.*;

/**
 * Editor for IPATranscript tiers 
 */
@TierEditorInfo(type=IPATranscript.class)
public class IPATierEditorExtension implements IPluginExtensionPoint<TierEditor> {

	@Override
	public Class<?> getExtensionType() {
		return TierEditor.class;
	}

	@Override
	public IPluginExtensionFactory<TierEditor> getFactory() {
		return factory;
	}
	
	private final IPluginExtensionFactory<TierEditor> factory = new IPluginExtensionFactory<TierEditor>() {
		
		@Override
		public TierEditor createObject(Object... args) {
			final SessionEditor editor = SessionEditor.class.cast(args[TierEditorFactory.EDITOR]);
			final Record record = Record.class.cast(args[TierEditorFactory.RECORD]);
			final Tier<?> tier = Tier.class.cast(args[TierEditorFactory.TIER]);
			final Integer group = Integer.class.cast(args[TierEditorFactory.GROUP]);
			
			if(tier.getDeclaredType() != IPATranscript.class) {
				throw new IllegalArgumentException("Tier type must be " + IPATranscript.class.getName());
			}
			
			@SuppressWarnings("unchecked")
			final Tier<IPATranscript> ipaTier = (Tier<IPATranscript>)tier;

			final Tier<PhoneMap> alignmentTier = record.getPhoneAlignment();
			PhoneMap alignment = alignmentTier.getGroup(group);
			
			Syllabifier syllabifier = null;
			final SyllabifierInfo info = editor.getSession().getExtension(SyllabifierInfo.class);
			if(info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null) {
				syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(
						info.getSyllabifierLanguageForTier(tier.getName()));
			}
			
			IPAGroupField retVal = new IPAGroupField(ipaTier, group, editor.getDataModel().getTranscriber(), syllabifier);

			IPAFieldTooltip tooltip = new IPAFieldTooltip();
			tooltip.setAlignmentTier(alignmentTier);
			tooltip.install(retVal);
			tooltip.addPropertyChangeListener(SyllabificationDisplay.SYLLABIFICATION_PROP_ID, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					final SyllabificationDisplay.SyllabificationChangeData newVal = (SyllabificationDisplay.SyllabificationChangeData)evt.getNewValue();
					final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
					final ScTypeEdit edit = new ScTypeEdit(editor, display.getTranscript(), newVal.getPosition(), newVal.getScType());
					editor.getUndoSupport().postEdit(edit);
				}

			});

			tooltip.addPropertyChangeListener(SyllabificationDisplay.HIATUS_CHANGE_PROP_ID, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
					final ToggleDiphthongEdit edit = new ToggleDiphthongEdit(editor, display.getTranscript(), (Integer)evt.getNewValue());
					editor.getUndoSupport().postEdit(edit);
				}

			});

			tooltip.addPropertyChangeListener(PhoneMapDisplay.ALIGNMENT_CHANGE_PROP, new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					final PhoneMapDisplay.AlignmentChangeData newVal = (PhoneMapDisplay.AlignmentChangeData)evt.getNewValue();
					final PhoneMapDisplay display = (PhoneMapDisplay)evt.getSource();

					final PhoneMap pm = new PhoneMap(record.getIPATarget().getGroup(group), record.getIPAActual().getGroup(group));
					pm.setTopAlignment(newVal.getAlignment()[0]);
					pm.setBottomAlignment(newVal.getAlignment()[1]);

					final TierEdit<PhoneMap> edit = new TierEdit<PhoneMap>(editor, alignmentTier, group, pm);
					edit.setFireHardChangeOnUndo(true);
					editor.getUndoSupport().postEdit(edit);
				}

			});


			return retVal;
		}
		
	};
	
}

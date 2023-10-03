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
package ca.phon.app.session.editor.view.ipa_validation;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.ipa_validation.actions.AutoValidateAction;
import ca.phon.ipa.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.syllabifier.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Editor view used to validate double-blind transcriptions.  References
 * the extension {@link AlternativeTranscript} in {@link IPATranscript}
 * objects for the IPA Target and IPA Actual tiers or a {@link Record}.
 */
public class ValidationEditorView extends EditorView {

	public final static String VIEW_NAME = "IPA Validation";
	
	public final static String VIEW_ICON = "misc/validation";
	
	private final TierDataLayoutPanel targetValidationPanel;
	
	private final Tier<IPATranscript> targetCandidateTier;
	
	private final TierDataLayoutPanel actualValidationPanel;
	
	private final Tier<IPATranscript> actualCandidateTier;
		
	private JButton autoValidateButton;

	public ValidationEditorView(SessionEditor editor) {
		super(editor);
		
		final SessionFactory factory = SessionFactory.newFactory();
		targetCandidateTier = factory.createTier("Target Validation", IPATranscript.class);
		actualCandidateTier = factory.createTier("Actual Validation", IPATranscript.class);
		
		targetValidationPanel = new TierDataLayoutPanel();
		targetValidationPanel.getTierLayout().setLayoutType(TierDataLayoutType.ALIGN_GROUPS);
		actualValidationPanel = new TierDataLayoutPanel();
		actualValidationPanel.getTierLayout().setLayoutType(TierDataLayoutType.ALIGN_GROUPS);
		init();
		update();
		setupEditorActions();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		autoValidateButton = new JButton(new AutoValidateAction(getEditor(), this));
		toolBar.add(autoValidateButton);
		toolBar.addSeparator();
		
		final PhonUIAction<Void> setTargetAct = PhonUIAction.runnable(this::onValidateIPATarget);
		setTargetAct.putValue(PhonUIAction.NAME, "Validate IPA Target");
		setTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Target using current candidate transcription");
		final JButton setTargetBtn = new JButton(setTargetAct);
		toolBar.add(setTargetBtn);
		
		final PhonUIAction<Void> setActualAct = PhonUIAction.runnable(this::onValidateIPAActual);
		setActualAct.putValue(PhonUIAction.NAME, "Validate IPA Actual");
		setActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Actual using current candidate transcription");
		final JButton setActualBtn = new JButton(setActualAct);
		toolBar.add(setActualBtn);
		
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(targetValidationPanel, BorderLayout.NORTH);
		panel.add(actualValidationPanel, BorderLayout.CENTER);
		final JScrollPane scroller = new JScrollPane(panel);
		
		add(toolBar, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
	}
	
	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}
	
	private void update() {
		final Record record = getEditor().currentRecord();
		if(record == null) return;
		
		updateCandidateTier(record.getIPATargetTier(), targetCandidateTier);
		updateValidationPanel(targetValidationPanel, record.getIPATargetTier(), targetCandidateTier);
		
		updateCandidateTier(record.getIPAActualTier(), actualCandidateTier);
		updateValidationPanel(actualValidationPanel, record.getIPAActualTier(), actualCandidateTier);
	}
	
	
	private void updateCandidateTier(Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		final IPATranscript t = tier.getValue();
		final AlternativeTranscript alts = t.getExtension(AlternativeTranscript.class);
		IPATranscript candidate = new IPATranscript();
		if(alts != null) {
			if(alts.getSelected() != null) {
				candidate = alts.get(alts.getSelected());
			} else if(alts.size() == 1) {
				final Iterator<String> keyItr = alts.keySet().iterator();
				while(keyItr.hasNext()) {
					final String key = keyItr.next();
					final IPATranscript tr = alts.get(key);
					if(candidate.length() == 0) {
						candidate = tr;
						alts.setSelected(key);
					} else {
						if(!candidate.toString().equals(tr.toString())) {
							candidate = new IPATranscript();
							alts.setSelected(null);
							break;
						}
					}
				}
			}
		}
		candidateTier.setValue(candidate);
	}
	
	private void updateValidationPanel(TierDataLayoutPanel panel, Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		panel.removeAll();
		
		final Session session = getEditor().getSession();
		final Transcribers transcribers = session.getTranscribers();
		
		int row = 0;
		final JLabel tierLabel = new JLabel("<html><b>" + tier.getName() + " Validation</b></html>");
		final TierDataConstraint tierLabelConstraint = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row);
		panel.add(tierLabel, tierLabelConstraint);
		
		int j = row + 1;
		for(Transcriber transcriber:transcribers) {
			final String tStr = (transcriber.getRealName() != null ? transcriber.getRealName() : transcriber.getUsername());
			final JLabel transLabel = new JLabel(tStr);
			transLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			final TierDataConstraint tdc = new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, j++);
			panel.add(transLabel, tdc);
		}
		
		// add candidate field object
		final IPAGroupField candidateField = new IPAGroupField(candidateTier);
		candidateField.setFont(FontPreferences.getTierFont());
		candidateField.addTierEditorListener(tierListener);

		final SetValueData data = new SetValueData();
		data.tier = tier;
		data.candidateTier = candidateTier;

		final PhonUIAction<SetValueData> setGrpAct = PhonUIAction.consumer(this::onSetValue, data);
		setGrpAct.putValue(PhonUIAction.NAME, "Set");
		setGrpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set group IPA");
		final JButton btn = new JButton(setGrpAct);

		final JPanel p = new JPanel(new HorizontalLayout(3));
		p.add(candidateField);
		p.add(btn);
		p.setOpaque(false);

		final TierDataConstraint candidateRestraint = new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row);
		panel.add(p, candidateRestraint);

		final IPATranscript ipa = tier.getValue();
		final AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
		final ButtonGroup btnGrp = new ButtonGroup();
		j = row;
		for(Transcriber t:transcribers) {
			final IPATranscript opt = (alts != null && alts.containsKey(t.getUsername()) ? alts.get(t.getUsername()) : new IPATranscript());

			final SelectIPAData selectData = new SelectIPAData();
			selectData.tier = tier;
			selectData.candidateTier = candidateTier;
			selectData.transcriber = t.getUsername();

			final PhonUIAction<SelectIPAData> optAct = PhonUIAction.consumer(this::onSelectIPA, selectData);
			optAct.putValue(PhonUIAction.NAME, opt.toString());
			if(alts != null && alts.getSelected() != null && alts.getSelected().equals(t.getUsername())) {
				optAct.putValue(PhonUIAction.SELECTED_KEY, true);
			}

			final JRadioButton optBtn = new JRadioButton(optAct);
			optBtn.setFont(FontPreferences.getTierFont());
			optBtn.setOpaque(false);
			btnGrp.add(optBtn);
			final TierDataConstraint tdc = new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, ++j);
			panel.add(optBtn, tdc);
		}
		panel.revalidate();
		panel.repaint();
	}
	
	/*
	 * UI actions
	 */
	
	public void onValidateIPATarget() {
		final Record r = getEditor().currentRecord();
		final Tier<IPATranscript> tier = r.getIPATargetTier();
		validateTier(tier, targetCandidateTier);
	}
	
	public void onValidateIPAActual() {
		final Record r = getEditor().currentRecord();
		final Tier<IPATranscript> tier = r.getIPAActualTier();
		validateTier(tier, actualCandidateTier);
	}

	/**
	 * Data passed to onSetGroup method
	 */
	public final class SetValueData {
		Tier<IPATranscript> tier;
		Tier<IPATranscript> candidateTier;
	}
	
	private void validateTier(Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		getEditor().getUndoSupport().beginUpdate();
		final SetValueData data = new SetValueData();
		data.tier = tier;
		data.candidateTier = candidateTier;

		final Tuple<TierEdit<IPATranscript>, TierEdit<PhoneAlignment>> edits = setValue(data);
		edits.getObj1().setFireHardChangeOnUndo(true);
		edits.getObj2().setFireHardChangeOnUndo(true);
		getEditor().getUndoSupport().postEdit(edits.getObj1());
		getEditor().getUndoSupport().postEdit(edits.getObj2());
		getEditor().getUndoSupport().endUpdate();
	}
	
	private Tuple<TierEdit<IPATranscript>, TierEdit<PhoneAlignment>> setValue(SetValueData data) {
		final Tier<IPATranscript> tier = data.tier;
		final IPATranscript ipa = data.candidateTier.getValue();
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		final Language lang = 
				(info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null ? 
						info.getSyllabifierLanguageForTier(tier.getName()) : library.defaultSyllabifierLanguage());
		final Syllabifier syllabifier = library.getSyllabifierForLanguage(lang);
		if(syllabifier != null) {
			syllabifier.syllabify(ipa.toList());
		}
		ipa.putExtension(AlternativeTranscript.class, tier.getValue().getExtension(AlternativeTranscript.class));
		
		final TierEdit<IPATranscript> edit = new TierEdit<IPATranscript>(getEditor(), getEditor().currentRecord(), data.tier, ipa);
		
		final Record r = getEditor().currentRecord();
		final Tier<PhoneAlignment> alignmentTier = r.getPhoneAlignmentTier();
		final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
		final TierEdit<PhoneAlignment> pmEdit = new TierEdit<>(getEditor(), alignmentTier, phoneAlignment);

		return new Tuple<>(edit, pmEdit);
	}
	
	/**
	 * Set ipa for a group
	 * @param data
	 */
	public void onSetValue(SetValueData data) {
		final Tuple<TierEdit<IPATranscript>, TierEdit<PhoneAlignment>> edits = setValue(data);
		getEditor().getUndoSupport().beginUpdate();
		edits.getObj1().setFireHardChangeOnUndo(true);
		edits.getObj2().setFireHardChangeOnUndo(true);
		getEditor().getUndoSupport().postEdit(edits.getObj1());
		getEditor().getUndoSupport().postEdit(edits.getObj2());
		getEditor().getUndoSupport().endUpdate();
	}
	
	/**
	 * Data passed to onSelectIPA method
	 */
	public final class SelectIPAData {
		Tier<IPATranscript> tier;
		Tier<IPATranscript> candidateTier;
		int group;
		String transcriber;
	}
	/**
	 * Select IPA for specified transcriber
	 * 
	 * @param data
	 */
	public void onSelectIPA(SelectIPAData data) {
		final IPATranscript grp = data.tier.getValue();
		final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
		if(alts != null && alts.containsKey(data.transcriber)) {
			final IPATranscript selected = alts.get(data.transcriber);
			alts.setSelected(data.transcriber);
			data.candidateTier.setValue(selected);
		}
	}
	
	/*
	 * Editor actions
	 */
	private void onSessionChanged(EditorEvent<Session> ee) {
		update();
	}

	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		update();
	}
	
	private final TierEditorListener<IPATranscript> tierListener = (tier, newValue, oldValue, valueIsAdjusting) -> {
		if(valueIsAdjusting) {
			final TierEdit<IPATranscript> edit = new TierEdit<>(getEditor(), tier, newValue);
			getEditor().getUndoSupport().postEdit(edit);
		} else {
			final EditorEvent<EditorEventType.TierChangeData> ee = new EditorEvent<>(EditorEventType.TierChanged, ValidationEditorView.this,
					new EditorEventType.TierChangeData(tier, oldValue, newValue));
			getEditor().getEventManager().queueEvent(ee);
		}
	};
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon(VIEW_ICON, IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		
		retVal.add(new AutoValidateAction(getEditor(), this));
		retVal.addSeparator();
		
		final PhonUIAction<Void> setTargetAct = PhonUIAction.runnable(this::onValidateIPATarget);
		setTargetAct.putValue(PhonUIAction.NAME, "Validate IPA Target");
		setTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Target using current candidate transcription");
		retVal.add(setTargetAct);
		
		final PhonUIAction<Void> setActualAct = PhonUIAction.runnable(this::onValidateIPAActual);
		setActualAct.putValue(PhonUIAction.NAME, "Validate IPA Actual");
		setActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Actual using current candidate transcription");
		retVal.add(setActualAct);
		
		return retVal;
	}

	
}

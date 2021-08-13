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

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.undo.*;

import org.jdesktop.swingx.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.ipa_validation.actions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllabifier.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Editor view used to validate double-blind transcriptions.  References
 * the extension {@link AlternativeTranscript} in {@link IPATranscript}
 * objects for the IPA Target and IPA Actual tiers or a {@link Record}.
 */
public class ValidationEditorView extends EditorView {

	private static final long serialVersionUID = -1179165192834735478L;

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
		targetCandidateTier = factory.createTier("Target Validation", IPATranscript.class, true);
		actualCandidateTier = factory.createTier("Actual Validation", IPATranscript.class, true);
		
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
		
		final PhonUIAction setTargetAct = new PhonUIAction(this, "onValidateIPATarget");
		setTargetAct.putValue(PhonUIAction.NAME, "Validate IPA Target");
		setTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Target using current candidate transcription");
		final JButton setTargetBtn = new JButton(setTargetAct);
		toolBar.add(setTargetBtn);
		
		final PhonUIAction setActualAct = new PhonUIAction(this, "onValidateIPAActual");
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
		final DelegateEditorAction recordChangedAct = new DelegateEditorAction(this, "onRecordChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);

		final DelegateEditorAction sessionChangedAct = new DelegateEditorAction(this, "onSessionChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_CHANGED_EVT, sessionChangedAct);
	}
	
	private void update() {
		final Record record = getEditor().currentRecord();
		if(record == null) return;
		
		updateCandidateTier(record.getIPATarget(), targetCandidateTier);
		updateValidationPanel(targetValidationPanel, record.getIPATarget(), targetCandidateTier);
		
		updateCandidateTier(record.getIPAActual(), actualCandidateTier);
		updateValidationPanel(actualValidationPanel, record.getIPAActual(), actualCandidateTier);
	}
	
	
	private void updateCandidateTier(Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		candidateTier.removeAll();
		
		for(IPATranscript t:tier) {
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
			candidateTier.addGroup(candidate);
		}
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
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			// add candidate field object
			final IPAGroupField candidateField = new IPAGroupField(candidateTier, i);
			candidateField.setFont(FontPreferences.getTierFont());
			candidateField.addTierEditorListener(tierListener);
			
			final SetGroupData data = new SetGroupData();
			data.tier = tier;
			data.candidateTier = candidateTier;
			data.group = i;
			
			final PhonUIAction setGrpAct = new PhonUIAction(this, "onSetGroup", data);
			setGrpAct.putValue(PhonUIAction.NAME, "Set");
			setGrpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set group IPA");
			final JButton btn = new JButton(setGrpAct);

			final JPanel p = new JPanel(new HorizontalLayout(3));
			p.add(candidateField);
			p.add(btn);
			p.setOpaque(false);
			
			final TierDataConstraint candidateRestraint = new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+i, row);
			panel.add(p, candidateRestraint);

			final IPATranscript ipa = tier.getGroup(i);
			final AlternativeTranscript alts = ipa.getExtension(AlternativeTranscript.class);
			final ButtonGroup btnGrp = new ButtonGroup();
			j = row;
			for(Transcriber t:transcribers) {
				final IPATranscript opt = (alts != null && alts.containsKey(t.getUsername()) ? alts.get(t.getUsername()) : new IPATranscript());
				
				final SelectIPAData selectData = new SelectIPAData();
				selectData.tier = tier;
				selectData.candidateTier = candidateTier;
				selectData.group = i;
				selectData.transcriber = t.getUsername();
				
				final PhonUIAction optAct = new PhonUIAction(this, "onSelectIPA", selectData);
				optAct.putValue(PhonUIAction.NAME, opt.toString());
				if(alts != null && alts.getSelected() != null && alts.getSelected().equals(t.getUsername())) {
					optAct.putValue(PhonUIAction.SELECTED_KEY, true);
				}
				
				final JRadioButton optBtn = new JRadioButton(optAct);
				optBtn.setFont(FontPreferences.getTierFont());
				optBtn.setOpaque(false);
				btnGrp.add(optBtn);
				final TierDataConstraint tdc = new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+i, ++j);
				panel.add(optBtn, tdc);
			}
		}
		panel.revalidate();
		panel.repaint();
	}
	
	/*
	 * UI actions
	 */
	
	public void onValidateIPATarget() {
		final Record r = getEditor().currentRecord();
		final Tier<IPATranscript> tier = r.getIPATarget();
		validateTier(tier, targetCandidateTier);
	}
	
	public void onValidateIPAActual() {
		final Record r = getEditor().currentRecord();
		final Tier<IPATranscript> tier = r.getIPAActual();
		validateTier(tier, actualCandidateTier);
	}

	/**
	 * Data passed to onSetGroup method
	 */
	public final class SetGroupData {
		Tier<IPATranscript> tier;
		Tier<IPATranscript> candidateTier;
		int group;
	}
	
	private void validateTier(Tier<IPATranscript> tier, Tier<IPATranscript> candidateTier) {
		final CompoundEdit edit = new CompoundEdit();
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			final SetGroupData data = new SetGroupData();
			data.tier = tier;
			data.candidateTier = candidateTier;
			data.group = i;
			edit.addEdit(setGroup(data));
		}
		
		edit.end();
		getEditor().getUndoSupport().postEdit(edit);
		
		// force update of tier in other views
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, this, tier.getName());
		getEditor().getEventManager().queueEvent(ee);
	}
	
	private UndoableEdit setGroup(SetGroupData data) {
		final Tier<IPATranscript> tier = data.tier;
		final IPATranscript ipa = data.candidateTier.getGroup(data.group);
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		final Language lang = 
				(info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null ? 
						info.getSyllabifierLanguageForTier(tier.getName()) : library.defaultSyllabifierLanguage());
		final Syllabifier syllabifier = library.getSyllabifierForLanguage(lang);
		if(syllabifier != null) {
			syllabifier.syllabify(ipa.toList());
		}
		ipa.putExtension(AlternativeTranscript.class, tier.getGroup(data.group).getExtension(AlternativeTranscript.class));
		
		final CompoundEdit cmpEdit = new CompoundEdit();
		
		final TierEdit<IPATranscript> edit = new TierEdit<IPATranscript>(getEditor(),
				data.tier, data.group, ipa);
		edit.doIt();
		cmpEdit.addEdit(edit);
		
		final Record r = getEditor().currentRecord();
		final Tier<PhoneMap> alignmentTier = r.getPhoneAlignment();
		final PhoneAligner aligner = new PhoneAligner();
		final PhoneMap pm = aligner.calculatePhoneMap(r.getIPATarget().getGroup(data.group), r.getIPAActual().getGroup(data.group));
		
		final TierEdit<PhoneMap> pmEdit = new TierEdit<PhoneMap>(getEditor(), alignmentTier, data.group, pm);
		pmEdit.doIt();
		cmpEdit.addEdit(pmEdit);
		
		cmpEdit.end();
		
		return cmpEdit;
	}
	
	/**
	 * Set ipa for a group
	 * @param data
	 */
	public void onSetGroup(SetGroupData data) {
		final UndoableEdit edit = setGroup(data);
		getEditor().getUndoSupport().postEdit(edit);
		
		// force update of tier in other views
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, this, data.tier.getName());
		getEditor().getEventManager().queueEvent(ee);
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
		final IPATranscript grp = data.tier.getGroup(data.group);
		final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
		if(alts != null && alts.containsKey(data.transcriber)) {
			final IPATranscript selected = alts.get(data.transcriber);
			alts.setSelected(data.transcriber);
			
			data.candidateTier.setGroup(data.group, selected);
		}
	}
	
	/*
	 * Editor actions
	 */
	@RunOnEDT
	public void onSessionChanged(EditorEvent ee) {
		update();
	}

	@RunOnEDT
	public void onRecordChanged(EditorEvent ee) {
		update();
	}
	
	private final TierEditorListener tierListener = new TierEditorListener() {
		
		@Override
		public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> edit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			getEditor().getUndoSupport().postEdit(edit);
		}

		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex,
				T newValue, T oldValue) {
			final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, ValidationEditorView.this, tier.getName());
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
		
		final PhonUIAction setTargetAct = new PhonUIAction(this, "onValidateIPATarget");
		setTargetAct.putValue(PhonUIAction.NAME, "Validate IPA Target");
		setTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Target using current candidate transcription");
		retVal.add(setTargetAct);
		
		final PhonUIAction setActualAct = new PhonUIAction(this, "onValidateIPAActual");
		setActualAct.putValue(PhonUIAction.NAME, "Validate IPA Actual");
		setActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set value of IPA Actual using current candidate transcription");
		retVal.add(setActualAct);
		
		return retVal;
	}

	
}

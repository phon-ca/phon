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
package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.app.ipalookup.OrthoLookupVisitor;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.Orthography;
import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.alignment.TierAlignmentRules;
import ca.phon.syllabifier.*;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.HorizontalLayout;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a UI for selecting IPA transcriptions from
 * dictionaries for the current record.
 *
 */
public class RecordLookupPanel extends JPanel {

	private final AtomicReference<Record> recordRef = new AtomicReference<Record>();

	final Tier<IPATranscript> lookupTier;

	private AtomicReference<IPADictionary> dictRef = new AtomicReference<IPADictionary>();

	private final WeakReference<SessionEditor> editorRef;

	/*
	 * UI components
	 */
	private JCheckBox overwriteBox;
	private JCheckBox ipaTargetBox;
	private JCheckBox ipaActualBox;

	private JButton setButton;

	private TierDataLayoutPanel candidatePanel;
	private TierDataLayoutPanel groupPanel;

	private JPanel controlPanel;

	RecordLookupPanel(SessionEditor editor) {
		super();

		final SessionFactory factory = SessionFactory.newFactory();
		lookupTier = factory.createTier("IPA Lookup", IPATranscript.class, TierAlignmentRules.ipaTierRules());

		editorRef = new WeakReference<SessionEditor>(editor);

		init();
	}

	public SessionEditor getEditor() {
		return editorRef.get();
	}

	public Record getRecord() {
		return recordRef.get();
	}

	public void setRecord(Record record) {
		recordRef.getAndSet(record);
		update();
	}

	public IPADictionary getDictionary() {
		return this.dictRef.get();
	}

	public void setDictionary(IPADictionary lookupContext) {
		dictRef.getAndSet(lookupContext);
		update();
	}

	private void init() {
		setBackground(Color.white);

		setLayout(new BorderLayout());

		candidatePanel = new TierDataLayoutPanel();
		groupPanel = new TierDataLayoutPanel();
		final JScrollPane scroller = new JScrollPane(groupPanel);

		overwriteBox = new JCheckBox("Overwrite");
		overwriteBox.setSelected(false);
		overwriteBox.setOpaque(false);

		ipaTargetBox = new JCheckBox(SystemTierType.IPATarget.getName());
		ipaTargetBox.setOpaque(false);
		ipaTargetBox.setSelected(true);

		ipaActualBox = new JCheckBox(SystemTierType.IPAActual.getName());
		ipaActualBox.setOpaque(false);
		ipaActualBox.setSelected(true);

		final PhonUIAction<Void> setAct = PhonUIAction.runnable(this::onSetTranscription);
		setAct.putValue(PhonUIAction.NAME, "Set");
		setAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set transcription for selected tiers");
		setButton = new JButton(setAct);

		controlPanel = new JPanel(new HorizontalLayout(((TierDataLayout)groupPanel.getLayout()).getHorizontalGap()));
		controlPanel.setOpaque(false);
		controlPanel.add(overwriteBox);
		controlPanel.add(ipaTargetBox);
		controlPanel.add(ipaActualBox);

		add(candidatePanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
	}

	void update() {
		// reset our tier
		updateLookupTier();
		updatePanel();
	}

	void updateLookupTier() {
		lookupTier.clear();
		final Record r = getRecord();
		if(r == null) return;

		final Orthography ortho = r.getOrthography();
		final OrthoLookupVisitor orthoLookup = new OrthoLookupVisitor(getDictionary());
		ortho.accept(orthoLookup);
		final WordLookupVisitor visitor = new WordLookupVisitor(this);
		ortho.accept(visitor);

		if(getDictionary() != null) {
			List<IPluginExtensionPoint<IPALookupPostProcessor>> extPts =
					PluginManager.getInstance().getExtensionPoints(IPALookupPostProcessor.class);
			IPATranscript ipa = lookupTier.getValue();
			for (var extPt : extPts) {
				IPALookupPostProcessor postprocessor = extPt.getFactory().createObject();
				ipa = postprocessor.postProcess(getDictionary(), ortho.toString(), ipa);
			}
			lookupTier.setValue(ipa);
		}
	}

	private void updatePanel() {
		candidatePanel.removeAll();
		groupPanel.removeAll();

		final Record r = getRecord();
		if(r == null) return;

		int row = 0;
		candidatePanel.add(controlPanel, new TierDataConstraint(TierDataConstraint.FLAT_TIER_PREF_COLUMN, row));

		++row;
		final JLabel transLbl = new JLabel("Transcription");
		transLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		candidatePanel.add(transLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
		final IPAGroupField ipaField = new IPAGroupField(lookupTier);
		ipaField.setFont(FontPreferences.getTierFont());
		ipaField.addTierEditorListener(tierListener);
		candidatePanel.add(ipaField, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row++));
		candidatePanel.add(setButton, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row));

		// create group sections
		final Tier<Orthography> orthoTier = r.getOrthographyTier();
		final TierDataLayout groupLayout = (TierDataLayout)groupPanel.getLayout();
		final JLabel tLbl = new JLabel("Transcription");
		tLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		final JPanel pnl = new JPanel(new BorderLayout());
		pnl.setOpaque(false);
		pnl.add(tLbl, BorderLayout.EAST);
		groupPanel.add(pnl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));

		final IPAGroupField grpField = new IPAGroupField(lookupTier);
		grpField.setFont(FontPreferences.getTierFont());
		grpField.addTierEditorListener(tierListener);

		final PhonUIAction<Void> setGrpAct = PhonUIAction.runnable(this::onSetValue);
		setGrpAct.putValue(PhonUIAction.NAME, "Set");
		setGrpAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set transcription");
		final JButton setGrpBtn = new JButton(setGrpAct);

		final String colLayout = "pref, " + groupLayout.getHorizontalGap() + "px, pref";
		final FormLayout formLayout = new FormLayout(colLayout, "pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel grpPanel = new JPanel(formLayout);
		grpPanel.setOpaque(false);
		grpPanel.add(grpField, cc.xy(1, 1));
		grpPanel.add(setGrpBtn, cc.xy(3, 1));

		groupPanel.add(grpPanel, new TierDataConstraint(TierDataConstraint.FLAT_TIER_PREF_COLUMN, row));

		++row;
		final Orthography ortho = orthoTier.getValue();
		final OptionBoxVisitior visitor = new OptionBoxVisitior(this, groupPanel, row);
		ortho.accept(visitor);

		repaint();
	}

	public void onSetTranscription() {
		final Record r = getRecord();
		if(r == null) return;

		final SessionEditor editor = getEditor();
		if(editor == null) return;

		onSetValue();
	}

	public void onSetValue() {
		final Transcriber transcriber = getEditor().getDataModel().getTranscriber();
		final Record r = getRecord();
		if(r == null) return;

		final SessionEditor editor = getEditor();
		if(editor == null) return;

		final Tier<IPATranscript> ipaTarget = r.getIPATargetTier();
		final Tier<IPATranscript> ipaActual = r.getIPAActualTier();

		final IPATranscript ipa = lookupTier.getValue();
		final IPATranscript ipaA = (new IPATranscriptBuilder()).append(ipa.toString()).toIPATranscript();
		if(ipa.getExtension(UnvalidatedValue.class) != null) {
			final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
			final UnvalidatedValue uv2 = new UnvalidatedValue(uv.getValue(), new ParseException(uv.getParseError().getMessage(), uv.getParseError().getErrorOffset()));
			ipaA.putExtension(UnvalidatedValue.class, uv2);
		}

		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		final SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		if(info.getSyllabifierLanguageForTier(SystemTierType.IPATarget.getName()) != null) {
			final Syllabifier syllabifier = library.getSyllabifierForLanguage(info.getSyllabifierLanguageForTier(SystemTierType.IPATarget.getName()));
			if(syllabifier != null) syllabifier.syllabify(ipa.toList());
		}
		if(info.getSyllabifierLanguageForTier(SystemTierType.IPAActual.getName()) != null) {
			final Syllabifier syllabifier = library.getSyllabifierForLanguage(info.getSyllabifierLanguageForTier(SystemTierType.IPAActual.getName()));
			if(syllabifier != null) syllabifier.syllabify(ipaA.toList());
		}

		getEditor().getUndoSupport().beginUpdate();
		IPATranscript targetIpa = ipaTarget.getValue();
		if(ipaTargetBox.isSelected()) {
			if(transcriber != null) {
				boolean set = true;
				final AlternativeTranscript alts = targetIpa.getExtension(AlternativeTranscript.class);
				if(alts != null) {
					final IPATranscript oldIpa = alts.get(transcriber.getUsername());
					boolean hasData = 
							(oldIpa != null && oldIpa.length() > 0) 
							|| (oldIpa != null && oldIpa.getExtension(UnvalidatedValue.class) != null && oldIpa.getExtension(UnvalidatedValue.class).getValue().trim().length() > 0);
					set = (overwriteBox.isSelected() || !hasData);
				}
				if(set) {
					final BlindTierEdit blindEdit = new BlindTierEdit(getEditor(), ipaTarget, transcriber, ipa, targetIpa);
					getEditor().getUndoSupport().postEdit(blindEdit);
				}
			} else {
				IPATranscript oldIpa = ipaTarget.getValue();
				boolean hasData = 
						(oldIpa != null && oldIpa.length() > 0) 
						|| (oldIpa != null && oldIpa.getExtension(UnvalidatedValue.class) != null && oldIpa.getExtension(UnvalidatedValue.class).getValue().trim().length() > 0);
				if(overwriteBox.isSelected() || !hasData) {
					final AlternativeTranscript alts = targetIpa.getExtension(AlternativeTranscript.class);
					if(alts != null) ipa.putExtension(AlternativeTranscript.class, alts);

					final TierEdit<IPATranscript> ipaTargetEdit = new TierEdit<>(editor, ipaTarget, ipa);
					getEditor().getUndoSupport().postEdit(ipaTargetEdit);
					targetIpa = ipa;
				}
			}
		}

		IPATranscript actualIpa = ipaActual.getValue();
		if(ipaActualBox.isSelected()) {
			if(transcriber != null) {
				boolean set = true;
				final AlternativeTranscript alts = targetIpa.getExtension(AlternativeTranscript.class);
				if(alts != null) {
					final IPATranscript oldIpa = alts.get(transcriber.getUsername());
					boolean hasData = 
							(oldIpa != null && oldIpa.length() > 0) 
							|| (oldIpa != null && oldIpa.getExtension(UnvalidatedValue.class) != null && oldIpa.getExtension(UnvalidatedValue.class).getValue().trim().length() > 0);
					set = (overwriteBox.isSelected() || !hasData);
				}
				if(set) {
					final BlindTierEdit blindEdit = new BlindTierEdit(getEditor(), ipaActual, transcriber, ipa, actualIpa);
					getEditor().getUndoSupport().postEdit(blindEdit);
				}
			} else {
				IPATranscript oldIpa = ipaActual.getValue();
				boolean hasData = 
						(oldIpa != null && oldIpa.length() > 0) 
						|| (oldIpa != null && oldIpa.getExtension(UnvalidatedValue.class) != null && oldIpa.getExtension(UnvalidatedValue.class).getValue().trim().length() > 0);
				if(overwriteBox.isSelected() || !hasData) {
					final AlternativeTranscript alts = actualIpa.getExtension(AlternativeTranscript.class);
					if(alts != null) ipaA.putExtension(AlternativeTranscript.class, alts);

					final TierEdit<IPATranscript> ipaActualEdit = new TierEdit<>(editor, ipaActual, ipaA);
					getEditor().getUndoSupport().postEdit(ipaActualEdit);
					actualIpa = ipaA;
				}
			}
		}

		if(transcriber == null) {
			final PhoneAligner aligner = new PhoneAligner();
			final PhoneMap pm = aligner.calculatePhoneAlignment(targetIpa, actualIpa);

			final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
			final TierEdit<PhoneAlignment> pmEdit = new TierEdit<>(editor, r.getPhoneAlignmentTier(), phoneAlignment);
			pmEdit.setFireHardChangeOnUndo(true);
			getEditor().getUndoSupport().postEdit(pmEdit);
		}

		getEditor().getUndoSupport().endUpdate();
	}

	private final TierEditorListener<IPATranscript> tierListener = (tier, newValue, oldValue, valueIsAdjusting) ->  {
		if(valueIsAdjusting) {
			final TierEdit<IPATranscript> edit = new TierEdit<>(getEditor(), tier, newValue);
			getEditor().getUndoSupport().postEdit(edit);
		} else {
			final EditorEvent<EditorEventType.TierChangeData> ee =
					new EditorEvent<>(EditorEventType.TierChanged, RecordLookupPanel.this, new EditorEventType.TierChangeData(tier, oldValue, newValue));
			getEditor().getEventManager().queueEvent(ee);
		}
	};

}

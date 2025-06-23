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
package ca.phon.app.session.editor.view.syllabificationAlignment;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.syllabificationAlignment.actions.*;
import ca.phon.app.session.editor.view.transcript.*;
import ca.phon.app.session.editor.view.transcript.extensions.SyllabificationComponentFactory;
import ca.phon.app.session.editor.view.transcript.extensions.SyllabificationExtension;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.FlatButton;
import ca.phon.ui.IconStrip;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.ipa.*;
import ca.phon.ui.ipa.PhoneMapDisplay.AlignmentChangeData;
import ca.phon.ui.ipa.SyllabificationDisplay.SyllabificationChangeData;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.*;
import com.jgoodies.forms.layout.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.beans.*;
import java.util.List;
import java.util.*;

public class SyllabificationAlignmentEditorView extends EditorView {

	public record ScEditData(IPATranscript ipa, int eleIdx, SyllableConstituentType oldType, SyllableConstituentType newType) { }
	public final static EditorEventType<ScEditData> ScEdit = new EditorEventType<>(EditorEventName.MODIFICATION_EVENT + "_SC_TYPE_", ScEditData.class);

	public final static String VIEW_NAME = "Syllabification & Alignment";

	public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":indeterminate_question_box";

	private IconStrip toolbar;

	private final static String SHOW_TARGET_IPA = "SyllabificationAndAlignmentEditorView.showTargetIPA";
	private final static boolean DEFAULT_SHOW_TARGET_IPA = true;
	private JCheckBox targetIPABox;

	private final static String SHOW_ACTUAL_IPA = "SyllabificationAndAlignmentEditorView.showActualIPA";
	private final static boolean DEFAULT_SHOW_ACTUAL_IPA = true;
	private JCheckBox actualIPABox;

	private final static String SHOW_ALIGNMENT = "SyllabificationAndAlignmentEditorView.showAlignment";
	private final static boolean DEFAULT_SHOW_ALIGNMENT = true;
	private JCheckBox alignmentBox;

	private final static String COLOR_IN_ALIGNMENT = "SyllabificationAndAlignmentEditorView.colorInAlignment";
	private final static boolean DEFAULT_COLOR_IN_ALIGNMENT = false;
	private JCheckBox colorInAlignmentBox;

	private final static String SHOW_DIACRITICS = "SyllabificationAndAlignmentEditorView.showDiacritics";
	private final static boolean DEFAULT_SHOW_DIACRITICS = false;
	private JCheckBox showDiacriticsBox;

	private TranscriptScrollPane scrollPane;
	private TranscriptEditor editor;

	private SyllabificationExtension syllabificationExtension;

	public SyllabificationAlignmentEditorView(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}

	private void init() {
		// toolbar
		toolbar = new IconStrip(SwingConstants.HORIZONTAL);

		ImageIcon sigmaIcn = IconManager.getInstance().getIcon("misc/small_sigma", IconSize.SMALL);

		final PhonUIAction<Void> syllabifierSettingsAct = PhonUIAction.runnable(() -> {
			final JPopupMenu settingsMenu = new JPopupMenu();
			final MenuBuilder menuBuilder = new MenuBuilder(settingsMenu);
		});
		syllabifierSettingsAct.putValue(PhonUIAction.NAME, "Settings");
		syllabifierSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select syllabifier settings for session");
		syllabifierSettingsAct.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		syllabifierSettingsAct.putValue(FlatButton.ICON_NAME_PROP, "settings");
		syllabifierSettingsAct.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);

//		final SyllabifierInfo syllabifierInfo = getEditor().getSession().getExtension(SyllabifierInfo.class);
//		SyllabificationSettingsPanel popupPanel = new SyllabificationSettingsPanel(syllabifierInfo);
//		popupPanel.addPropertyChangeListener(SyllabificationSettingsPanel.IPA_TARGET_SYLLABIFIER_PROP, (e) -> {
//			syllabifierInfo.saveInfo(getEditor().getSession());
//		});
//		popupPanel.addPropertyChangeListener(SyllabificationSettingsPanel.IPA_ACTUAL_SYLLABIFIER_PROP, (e) -> {
//			syllabifierInfo.saveInfo(getEditor().getSession());
//		});
//		syllabifierSettingsAct.putValue(DropDownButton.BUTTON_POPUP, popupPanel);
//		syllabifierSettingsAct.putValue(DropDownButton.ARROW_ICON_GAP, 0);
//		syllabifierSettingsAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);

		FlatButton settingsBtn = new FlatButton(syllabifierSettingsAct);

		toolbar.add(settingsBtn, IconStrip.IconStripPosition.LEFT);

		setLayout(new BorderLayout());
//		scroller = new JScrollPane(contentPane);
//		scroller.setBackground(Color.white);
//		scroller.setOpaque(true);
		add(toolbar, BorderLayout.NORTH);
//		add(scroller, BorderLayout.CENTER);

		editor = new TranscriptEditor(getEditor().getDataModel(), getEditor().getSelectionModel(), getEditor().getEventManager(),
				getEditor().getUndoSupport(), getEditor().getUndoManager());

		editor.addAdditionalTierName(SystemTierType.TargetSyllables.getName());
		editor.addAdditionalTierName(SystemTierType.ActualSyllables.getName());
		editor.addAdditionalTierName(SystemTierType.PhoneAlignment.getName());
		editor.recalculateTierLabelWidth();

		editor.setAutoInsertRecordElements(false);
		editor.getTranscriptDocument().setSessionNoPopulate(getEditor().getSession());

		syllabificationExtension = new SyllabificationExtension();
		syllabificationExtension.install(editor);

		scrollPane = new TranscriptScrollPane(editor);
		add(scrollPane, BorderLayout.CENTER);

		update();
	}

	private void setupSettingsMenu(MenuBuilder menuBuilder) {
		final PhonUIAction<Void> toggleTargetAct = PhonUIAction.runnable(this::toggleCheckbox);
		toggleTargetAct.putValue(PhonUIAction.NAME, SystemTierType.TargetSyllables.getName());
		toggleTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle target syllables");
		toggleTargetAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SHOW_TARGET_IPA, DEFAULT_SHOW_TARGET_IPA));
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleTargetAct));

		final PhonUIAction<Void> toggleActualAct = PhonUIAction.runnable(this::toggleCheckbox);
		toggleActualAct.putValue(PhonUIAction.NAME, SystemTierType.ActualSyllables.getName());
		toggleActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle actual syllables");
		toggleActualAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SHOW_ACTUAL_IPA, DEFAULT_SHOW_ACTUAL_IPA));
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleActualAct));

		final PhonUIAction<Void> toggleAlignmentAct = PhonUIAction.runnable(this::toggleCheckbox);
		toggleAlignmentAct.putValue(PhonUIAction.NAME, SystemTierType.PhoneAlignment.getName());
		toggleAlignmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle alignment");
		toggleAlignmentAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SHOW_ALIGNMENT, DEFAULT_SHOW_ALIGNMENT));
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleAlignmentAct));

		final PhonUIAction<Void> toggleAlignmentColorAct = PhonUIAction.runnable(this::toggleCheckbox);
		toggleAlignmentColorAct.putValue(PhonUIAction.NAME, "Color in alignment");
		toggleAlignmentColorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle color in alignment");
		toggleAlignmentColorAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(COLOR_IN_ALIGNMENT, DEFAULT_COLOR_IN_ALIGNMENT));
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleAlignmentColorAct));

		final PhonUIAction<Void> toggleDiacriticsAct = PhonUIAction.runnable(this::toggleCheckbox);
		toggleDiacriticsAct.putValue(PhonUIAction.NAME, "Show diacritics");
		toggleDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle display of diacritics");
		toggleDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SHOW_DIACRITICS, DEFAULT_SHOW_DIACRITICS));
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleDiacriticsAct));
	}

	private void setupEditorActions() {
		final SessionEditor editor = getEditor();
		final EditorEventManager eventManager = editor.getEventManager();

		eventManager.registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		eventManager.registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		eventManager.registerActionForEvent(EditorEventType.RecordRefresh, this::onRecordChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		eventManager.registerActionForEvent(EditorEventType.TierChange, this::onTierChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		eventManager.registerActionForEvent(ScEdit, this::onScChange, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	private final PropertyChangeListener syllabificationDisplayListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final SyllabificationChangeData newVal = (SyllabificationChangeData)evt.getNewValue();
			final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
			final ScTypeEdit edit = new ScTypeEdit(getEditor(), display.getTranscript(), newVal.position(), newVal.scType());
			getEditor().getUndoSupport().postEdit(edit);
		}

	};

	private final PropertyChangeListener hiatusChangeListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final SyllabificationDisplay display = (SyllabificationDisplay)evt.getSource();
			final ToggleDiphthongEdit edit = new ToggleDiphthongEdit(getEditor(), display.getTranscript(), (Integer)evt.getNewValue());
			getEditor().getUndoSupport().postEdit(edit);
		}

	};

//	private final PropertyChangeListener alignmentDisplayListener = new PropertyChangeListener() {
//
//		@Override
//		public void propertyChange(PropertyChangeEvent evt) {
//			final AlignmentChangeData newVal = (AlignmentChangeData)evt.getNewValue();
//			final Record r = getEditor().currentRecord();
//			final int wIdx = newVal.wordIndex();
//			final List<IPATranscript> targetWords = targetDisplay.getTranscript().words();
//			final List<IPATranscript> actualWords = actualDisplay.getTranscript().words();
//			final IPATranscript ipaTarget = wIdx < targetWords.size() ? targetWords.get(wIdx) : new IPATranscript();
//			final IPATranscript ipaActual = wIdx < actualWords.size() ? actualWords.get(wIdx) : new IPATranscript();
//			final PhoneMap pm = new PhoneMap(ipaTarget, ipaActual);
//			pm.setTopAlignment(newVal.alignment()[0]);
//			pm.setBottomAlignment(newVal.alignment()[1]);
//
//			final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
//			final List<PhoneMap> modifiedAlignments = new ArrayList<>();
//			for(int i = 0; i < phoneAlignment.getAlignments().size(); i++) {
//				if(i == wIdx)
//					modifiedAlignments.add(pm);
//				else
//					modifiedAlignments.add(phoneAlignment.getAlignments().get(i));
//			}
//
//			final TierEdit<PhoneAlignment> edit = new TierEdit<>(getEditor(), r.getPhoneAlignmentTier(),
//					new PhoneAlignment(modifiedAlignments));
//			getEditor().getUndoSupport().postEdit(edit);
//		}
//
//	};

	public void update() {
		final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());

		final Record record = getEditor().currentRecord();
		if(record == null) return;

		final SimpleAttributeSet ipaTargetAttrs = new SimpleAttributeSet();
		TranscriptStyleConstants.setRecord(ipaTargetAttrs, record);
		TranscriptStyleConstants.setTier(ipaTargetAttrs, record.getIPATargetTier());

		final SimpleAttributeSet ipaActualAttrs = new SimpleAttributeSet();
		TranscriptStyleConstants.setRecord(ipaActualAttrs, record);
		TranscriptStyleConstants.setTier(ipaActualAttrs, record.getIPAActualTier());


		syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaTargetAttrs);
		syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaActualAttrs);

		try {
			editor.getTranscriptDocument().setBypassDocumentFilter(true);
			editor.getTranscriptDocument().remove(0, editor.getTranscriptDocument().getLength());
			editor.getTranscriptDocument().processBatchUpdates(0, batchBuilder.getBatch());
		} catch (BadLocationException e) {
			LogUtil.warning(e);
		} finally {
			editor.getTranscriptDocument().setBypassDocumentFilter(false);
		}
	}

	public void toggleCheckbox() {
		update();
		repaint();
	}

	/*---- Editor Actions -------------------*/
	private void onSessionChanged(EditorEvent<Session> ee) {
		onDataChanged(ee);
	}

	private void onRecordChanged(EditorEvent<EditorEventType.RecordChangedData> ee) {
		onDataChanged(ee);
	}

	private void onDataChanged(EditorEvent<?> ee) {
		update();
		repaint();
	}

	private void updateActualSyllables() {
		final Record r = getEditor().currentRecord();
		if(r == null) return;

		final int recordIndex = getEditor().getDataModel().getSession().getRecordIndex(r);
		if(recordIndex < 0) return;

		final TranscriptDocument.StartEnd actualSyllablesRange =
				editor.getTranscriptDocument().getTierStartEnd(recordIndex, SystemTierType.ActualSyllables.getName());
		if(actualSyllablesRange.valid()) {
			final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
			final SimpleAttributeSet ipaActualAttrs = new SimpleAttributeSet();
			TranscriptStyleConstants.setRecord(ipaActualAttrs, r);
			TranscriptStyleConstants.setTier(ipaActualAttrs, r.getIPAActualTier());
			syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaActualAttrs);
			try {
				editor.getTranscriptDocument().setBypassDocumentFilter(true);
				editor.getTranscriptDocument().remove(actualSyllablesRange.start(), actualSyllablesRange.length());
				editor.getTranscriptDocument().processBatchUpdates(actualSyllablesRange.start(), batchBuilder.getBatch());
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			} finally {
				editor.getTranscriptDocument().setBypassDocumentFilter(false);
			}
		}
	}

	private void updateTargetSyllables() {
		final Record r = getEditor().currentRecord();
		if(r == null) return;

		final int recordIndex = getEditor().getDataModel().getSession().getRecordIndex(r);
		if(recordIndex < 0) return;

		final TranscriptDocument.StartEnd targetSyllablesRange =
				editor.getTranscriptDocument().getTierStartEnd(recordIndex, SystemTierType.TargetSyllables.getName());
		if(targetSyllablesRange.valid()) {
			final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
			final SimpleAttributeSet ipaTargetAttrs = new SimpleAttributeSet();
			TranscriptStyleConstants.setRecord(ipaTargetAttrs, r);
			TranscriptStyleConstants.setTier(ipaTargetAttrs, r.getIPATargetTier());
			syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaTargetAttrs);
			try {
				editor.getTranscriptDocument().setBypassDocumentFilter(true);
				editor.getTranscriptDocument().remove(targetSyllablesRange.start(), targetSyllablesRange.length());
				editor.getTranscriptDocument().processBatchUpdates(targetSyllablesRange.start(), batchBuilder.getBatch());
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			} finally {
				editor.getTranscriptDocument().setBypassDocumentFilter(false);
			}
		}
	}

	private void onTierChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
		final String tierName = ee.data().tier().getName();
		if(SystemTierType.IPATarget.getName().equals(tierName)) {
			updateTargetSyllables();
		} else if(SystemTierType.IPAActual.getName().equals(tierName)) {
			updateActualSyllables();
		} else if(SystemTierType.PhoneAlignment.getName().equals(tierName)) {
//			final Record r = getEditor().currentRecord();
//			if(r != null) {
//				final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(r.getIPATargetTier(), r.getIPAActualTier());
//				r.setPhoneAlignment(phoneAlignment);
//			}
		}
	}

	private void onScChange(EditorEvent<ScEditData> ee) {
//		final IPATranscript ipa = ee.data().ipa();
//		final Record r = getEditor().currentRecord();
//		final SyllabificationDisplay targetDisplay = getIPATargetDisplay();
//		final SyllabificationDisplay actualDisplay = getIPAActualDisplay();
//		boolean found = false;
//		if(targetDisplay.getTranscript() == ipa) {
//			targetDisplay.repaint();
//			found = true;
//		} else if(actualDisplay.getTranscript() == ipa) {
//			actualDisplay.repaint();
//			found = true;
//		}
//		if(found) {
//			getAlignmentDisplay().repaint();
//		}
	}

	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/syllabification", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();

		retVal.add(new SyllabificationSettingsCommand(getEditor(), this));

		retVal.addSeparator();

		final ResetSyllabificationCommand resetIPATargetAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPATarget.getName());
		retVal.add(resetIPATargetAct);

		final ResetSyllabificationCommand resetIPAActualAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPAActual.getName());
		retVal.add(resetIPAActualAct);

		final ResetAlignmentCommand resetAlignmentAct = new ResetAlignmentCommand(getEditor(), this);
		retVal.add(resetAlignmentAct);

		return retVal;
	}

}

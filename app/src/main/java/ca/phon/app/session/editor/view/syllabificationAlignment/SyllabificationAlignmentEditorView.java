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
import ca.phon.app.session.editor.view.transcript.extensions.AlignmentComponentFactory;
import ca.phon.app.session.editor.view.transcript.extensions.AlignmentExtension;
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
import org.w3c.dom.Attr;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.*;
import java.util.List;
import java.util.*;

public class SyllabificationAlignmentEditorView extends EditorView {

	public record ScEditData(int transcriptElementIdx, String tier, IPATranscript ipa, int eleIdx, SyllableConstituentType oldType, SyllableConstituentType newType) { }
	public final static EditorEventType<ScEditData> ScEdit = new EditorEventType<>(EditorEventName.MODIFICATION_EVENT + "_SC_TYPE_", ScEditData.class);

	public final static String VIEW_NAME = "Syllabification & Alignment";

	public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":flex_wrap";

	private IconStrip toolbar;

	private final static String SHOW_TARGET_IPA = "showTargetIPA";
	private final static boolean DEFAULT_SHOW_TARGET_IPA = true;
	private boolean showTargetIPA = DEFAULT_SHOW_TARGET_IPA;

	private final static String SHOW_ACTUAL_IPA = "showActualIPA";
	private final static boolean DEFAULT_SHOW_ACTUAL_IPA = true;
	private boolean showActualIPA = DEFAULT_SHOW_ACTUAL_IPA;

	private final static String SHOW_ALIGNMENT = "showAlignment";
	private final static boolean DEFAULT_SHOW_ALIGNMENT = true;
	private boolean showAlignment = DEFAULT_SHOW_ALIGNMENT;

	private final static String COLOR_IN_ALIGNMENT = "colorInAlignment";
	private final static boolean DEFAULT_COLOR_IN_ALIGNMENT = false;
	private boolean colorInAlignment = DEFAULT_COLOR_IN_ALIGNMENT;

	private final static String SHOW_DIACRITICS = "showDiacritics";
	private final static boolean DEFAULT_SHOW_DIACRITICS = false;
	private boolean showDiacritics = DEFAULT_SHOW_DIACRITICS;

//	private TranscriptScrollPane scrollPane;
	private TranscriptEditor editor;

	private SyllabificationExtension syllabificationExtension;

	private AlignmentExtension alignmentExtension;

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
			setupSettingsMenu(menuBuilder);
			settingsMenu.show(toolbar, 0, toolbar.getHeight());
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
		editor.getTranscriptDocument().putDocumentProperty(AlignmentExtension.ALIGNMENT_PARENT, SystemTierType.IPAActual.getName());

		syllabificationExtension = editor.getExtension(SyllabificationExtension.class);
		if(syllabificationExtension == null) {
			syllabificationExtension = new SyllabificationExtension();
			syllabificationExtension.install(editor);
		}

		alignmentExtension = editor.getExtension(AlignmentExtension.class);
		if(alignmentExtension == null) {
			alignmentExtension = new AlignmentExtension();
			alignmentExtension.install(editor);
		}

		JScrollPane scrollPane = new JScrollPane(editor);
		add(scrollPane, BorderLayout.CENTER);

		update();
	}

	private void setupSettingsMenu(MenuBuilder menuBuilder) {
		final PhonUIAction<Void> toggleTargetAct = PhonUIAction.runnable(this::toggleShowIPATarget);
		toggleTargetAct.putValue(PhonUIAction.NAME, SystemTierType.TargetSyllables.getName());
		toggleTargetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle target syllables");
		toggleTargetAct.putValue(PhonUIAction.SELECTED_KEY, showTargetIPA);
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleTargetAct));

		final PhonUIAction<Void> toggleActualAct = PhonUIAction.runnable(this::toggleShowIPAActual);
		toggleActualAct.putValue(PhonUIAction.NAME, SystemTierType.ActualSyllables.getName());
		toggleActualAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle actual syllables");
		toggleActualAct.putValue(PhonUIAction.SELECTED_KEY, showActualIPA);
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleActualAct));

		final PhonUIAction<Void> toggleAlignmentAct = PhonUIAction.runnable(this::toggleShowAlignment);
		toggleAlignmentAct.putValue(PhonUIAction.NAME, SystemTierType.PhoneAlignment.getName());
		toggleAlignmentAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle alignment");
		toggleAlignmentAct.putValue(PhonUIAction.SELECTED_KEY, showAlignment);
		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleAlignmentAct));

//		final PhonUIAction<Void> toggleAlignmentColorAct = PhonUIAction.runnable(this::toggleColorInAlignment);
//		toggleAlignmentColorAct.putValue(PhonUIAction.NAME, "Color in alignment");
//		toggleAlignmentColorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle color in alignment");
//		toggleAlignmentColorAct.putValue(PhonUIAction.SELECTED_KEY, colorInAlignment);
//		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleAlignmentColorAct));
//
//		final PhonUIAction<Void> toggleDiacriticsAct = PhonUIAction.runnable(this::toggleShowDiacritics);
//		toggleDiacriticsAct.putValue(PhonUIAction.NAME, "Show diacritics");
//		toggleDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle display of diacritics");
//		toggleDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, showDiacritics);
//		menuBuilder.addItem(".", new JCheckBoxMenuItem(toggleDiacriticsAct));

		menuBuilder.addSeparator(".", "separator");

		setupMenu(menuBuilder);
	}

	private boolean toggleShowIPATarget() {
		showTargetIPA = !showTargetIPA;
		if(showTargetIPA) {
			editor.addAdditionalTierName(SystemTierType.TargetSyllables.getName());
		} else {
			editor.removeAdditionalTierName(SystemTierType.TargetSyllables.getName());
		}
		update();
		return showTargetIPA;
	}

	private boolean toggleShowIPAActual() {
		showActualIPA = !showActualIPA;
		if(showActualIPA) {
			editor.addAdditionalTierName(SystemTierType.ActualSyllables.getName());
		} else {
			editor.removeAdditionalTierName(SystemTierType.ActualSyllables.getName());
		}
		firePropertyChange(SHOW_TARGET_IPA, !showTargetIPA, showTargetIPA);
		update();
		return showActualIPA;
	}

	private boolean toggleShowAlignment() {
		showAlignment = !showAlignment;
		if(showAlignment) {
			editor.addAdditionalTierName(SystemTierType.PhoneAlignment.getName());
		} else {
			editor.removeAdditionalTierName(SystemTierType.PhoneAlignment.getName());
		}
		firePropertyChange(SHOW_ALIGNMENT, !showAlignment, showAlignment);
		update();
		return showAlignment;
	}

	private boolean toggleColorInAlignment() {
		// TODO
		return colorInAlignment;
	}

	private boolean toggleShowDiacritics() {
		// TODO
		return showDiacritics;
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

		if(showTargetIPA)
			syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaTargetAttrs);
		if(showActualIPA)
			syllabificationExtension.buildSyllabificationBatch(batchBuilder, ipaActualAttrs);
		if(showAlignment)
			alignmentExtension.buildAlignmentBatch(batchBuilder, ipaActualAttrs);

		try {
			editor.getTranscriptEditorCaret().freeze();
			editor.getTranscriptDocument().setBypassDocumentFilter(true);
			editor.getTranscriptDocument().remove(0, editor.getTranscriptDocument().getLength());
			editor.getTranscriptDocument().processBatchUpdates(0, batchBuilder.getBatch());
		} catch (BadLocationException e) {
			LogUtil.warning(e);
		} finally {
			editor.getTranscriptDocument().setBypassDocumentFilter(false);
			editor.getTranscriptEditorCaret().unfreeze();
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
				editor.getTranscriptEditorCaret().freeze();
				editor.getTranscriptDocument().setBypassDocumentFilter(true);
				editor.getTranscriptDocument().remove(actualSyllablesRange.start(), actualSyllablesRange.length());
				editor.getTranscriptDocument().processBatchUpdates(actualSyllablesRange.start(), batchBuilder.getBatch());
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			} finally {
				editor.getTranscriptDocument().setBypassDocumentFilter(false);
				editor.getTranscriptEditorCaret().unfreeze();
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
				editor.getTranscriptEditorCaret().freeze();
				editor.getTranscriptDocument().setBypassDocumentFilter(true);
				editor.getTranscriptDocument().remove(targetSyllablesRange.start(), targetSyllablesRange.length());
				editor.getTranscriptDocument().processBatchUpdates(targetSyllablesRange.start(), batchBuilder.getBatch());
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			} finally {
				editor.getTranscriptDocument().setBypassDocumentFilter(false);
				editor.getTranscriptEditorCaret().unfreeze();
			}
		}
	}

	private void updateAlignment() {
		final Record r = getEditor().currentRecord();
		if(r == null) return;

		final int recordIndex = getEditor().getDataModel().getSession().getRecordIndex(r);
		if(recordIndex < 0) return;

		final TranscriptDocument.StartEnd alignRange =
				editor.getTranscriptDocument().getTierStartEnd(recordIndex, SystemTierType.PhoneAlignment.getName());
		if(alignRange.valid()) {
			final TranscriptBatchBuilder batchBuilder = new TranscriptBatchBuilder(editor.getTranscriptDocument());
			final SimpleAttributeSet ipaActualAttrs = new SimpleAttributeSet();
			TranscriptStyleConstants.setRecord(ipaActualAttrs, r);
			TranscriptStyleConstants.setTier(ipaActualAttrs, r.getIPAActualTier());
			alignmentExtension.buildAlignmentBatch(batchBuilder, ipaActualAttrs);
			try {
				editor.getTranscriptEditorCaret().freeze();
				editor.getTranscriptDocument().setBypassDocumentFilter(true);
				editor.getTranscriptDocument().remove(alignRange.start(), alignRange.length());
				editor.getTranscriptDocument().processBatchUpdates(alignRange.start(), batchBuilder.getBatch());
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			} finally {
				editor.getTranscriptDocument().setBypassDocumentFilter(false);
				editor.getTranscriptEditorCaret().unfreeze();
			}
		}
	}

	private void onTierChanged(EditorEvent<EditorEventType.TierChangeData> ee) {
		if(ee.data().valueAdjusting()) return;
		final String tierName = ee.data().tier().getName();
		if(SystemTierType.IPATarget.getName().equals(tierName)) {
			updateTargetSyllables();
		} else if(SystemTierType.IPAActual.getName().equals(tierName)) {
			updateActualSyllables();
		} else if(SystemTierType.PhoneAlignment.getName().equals(tierName)) {
			if(ee.source() != null) {
				final TranscriptDocument.StartEnd alignmentRange = editor.getTranscriptDocument().getTierContentStartEnd(
						getEditor().getSession().getRecordIndex(getEditor().currentRecord()), SystemTierType.PhoneAlignment.getName());
				if(alignmentRange.valid()) {
					final AttributeSet attrs = editor.getTranscriptDocument().getCharacterElement(alignmentRange.start()).getAttributes();
					final ComponentFactory componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
					if(componentFactory instanceof AlignmentComponentFactory) {
						if (componentFactory.getComponent() == ee.source().getParent())
							return;
					}
				}
			}
			updateAlignment();
		}
	}

	private void onScChange(EditorEvent<ScEditData> ee) {
		final IPATranscript ipa = ee.data().ipa();
		final Record r = getEditor().currentRecord();
		if(r == null) return;

		final Component source = ee.source();
		if(source instanceof SyllabificationDisplay syllabificationDisplay) {
			TranscriptDocument.StartEnd syllableRange = new TranscriptDocument.StartEnd(-1, -1);
			if(SystemTierType.IPATarget.getName().equals(ee.data().tier())) {
				syllableRange = editor.getTranscriptDocument().getTierContentStartEnd(getEditor().getSession().getRecordIndex(r), SystemTierType.TargetSyllables.getName());
			} else if(SystemTierType.IPAActual.getName().equals(ee.data().tier())) {
				syllableRange = editor.getTranscriptDocument().getTierContentStartEnd(getEditor().getSession().getRecordIndex(r), SystemTierType.ActualSyllables.getName());
			}
			if(syllableRange.valid()) {
				final AttributeSet attrs = editor.getTranscriptDocument().getCharacterElement(syllableRange.start()).getAttributes();
				final ComponentFactory componentFactory = TranscriptStyleConstants.getComponentFactory(attrs);
				if(componentFactory instanceof SyllabificationComponentFactory) {
					if (componentFactory.getComponent() == source.getParent())
						return;
				}
			}
		}

		if(SystemTierType.IPATarget.getName().equals(ee.data().tier)) {
			updateTargetSyllables();
		} else if(SystemTierType.IPAActual.getName().equals(ee.data().tier)) {
			updateActualSyllables();
		}
	}

	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		final String[] iconData = VIEW_ICON.split(":");
		return IconManager.getInstance().getFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, Color.darkGray);
	}

	private void setupMenu(MenuBuilder menuBuilder) {
		menuBuilder.addItem(".", new JMenuItem(new SyllabificationSettingsCommand(getEditor(), this)));

		menuBuilder.addSeparator(".", "separator");

		final ResetSyllabificationCommand resetIPATargetAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPATarget.getName());
		menuBuilder.addItem(".", new JMenuItem(resetIPATargetAct));

		final ResetSyllabificationCommand resetIPAActualAct = new ResetSyllabificationCommand(getEditor(), this, SystemTierType.IPAActual.getName());
		menuBuilder.addItem(".", new JMenuItem(resetIPAActualAct));

		final ResetAlignmentCommand resetAlignmentAct = new ResetAlignmentCommand(getEditor(), this);
		menuBuilder.addItem(".", new JMenuItem(resetAlignmentAct));
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		setupMenu(new MenuBuilder(retVal));
		return retVal;
	}

}

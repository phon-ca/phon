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
package ca.phon.app.session.editor.view.record_data;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.TierTransferrable;
import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.PlaySegmentAction;
import ca.phon.app.session.editor.search.FindAndReplacePanel;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.ipa.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.position.*;
import ca.phon.syllabifier.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Highlighter.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Editor view for tier data.
 *
 */
public class RecordDataEditorView extends EditorView implements ClipboardOwner {

	public final static String VIEW_NAME = "Record Data";

	public final static String VIEW_ICON = "misc/record";

	private JPanel topPanel;

	/**
	 * speaker selection
	 */
	private JComboBox<Participant> speakerBox;

	private volatile boolean updating = false;

	/**
	 * Font size
	 */
	private DropDownButton fontSizeButton;
	private JPopupMenu fontSizeMenu;

	public final static String FONT_SIZE_DELTA_PROP = RecordDataEditorView.class.getName() + ".fontSizeDelta";
	public final static float DEFAULT_FONT_SIZE_DELTA = 0.0f;
	public float fontSizeDelta = PrefHelper.getFloat(FONT_SIZE_DELTA_PROP, DEFAULT_FONT_SIZE_DELTA);

	/**
	 * query exclusion
	 */
	private JCheckBox excludeFromSearchesBox;
	private final static String excludeFromSearchesText = "Exclude";

	/**
	 * content pane
	 */
	private TierDataLayoutPanel contentPane;

	/**
	 * Status panel
	 */
	private JPanel statusPanel;

	private JLabel recordIdLbl;

	private JLabel currentTierLbl;

	private JLabel currentGroupLbl;

	private JLabel currentCharLbl;

	private RecordNumberField recNumField;
	
	private JButton playButton;
	
	/*
	 * Find and replace
	 */
	private JToggleButton findAndReplaceButton;
	private FindAndReplacePanel findAndReplacePanel;

	private CrossTierAlignmentView crossTierAlignmentView;

	/*
	 * Keep track of 'current' group and tier.  This is done by tracking
	 * focus of the various TierEditors.
	 */
	private final AtomicReference<Tier<?>> currentTierRef = new AtomicReference<Tier<?>>();

	private final AtomicInteger currentGroupIndex = new AtomicInteger(-1);

	private final AtomicInteger currentRecordIndex = new AtomicInteger(-1);

	private final AtomicInteger currentCharIndex = new AtomicInteger(-1);

	private final Map<String, TierEditor<?>> editorMap = new LinkedHashMap<>();

	public RecordDataEditorView(SessionEditor editor) {
		super(editor);
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		setBackground(Color.white);

		contentPane = new TierDataLayoutPanel();
		contentPane.setBackground(Color.white);

		final JScrollPane scroller = new JScrollPane(contentPane);
		scroller.setBackground(Color.white);
		add(scroller, BorderLayout.CENTER);

		final JPanel panel = getTopPanel();
		add(panel, BorderLayout.NORTH);

		final FormLayout statusLayout =
				new FormLayout("pref, pref, fill:pref:grow, "
						+ "right:pref, right:pref, right:pref, right:pref, right:pref, right:pref", "pref");
		final CellConstraints cc = new CellConstraints();
		statusPanel = new JPanel(statusLayout);

		final Font font = Font.decode("monospace-PLAIN-10");
		final JLabel idLbl = new JLabel("Id: ");
		idLbl.setFont(font);

		recordIdLbl = new JLabel();
		recordIdLbl.setFont(font);

		statusPanel.add(idLbl, cc.xy(1,1));
		statusPanel.add(recordIdLbl, cc.xy(2,1));

		final JLabel tierLbl = new JLabel("Tier: ");
		tierLbl.setFont(font);

		currentTierLbl = new JLabel();
		currentTierLbl.setFont(font);

		statusPanel.add(tierLbl, cc.xy(4,1));
		statusPanel.add(currentTierLbl, cc.xy(5,1));

		final JLabel grpLbl = new JLabel(" Group: ");
		grpLbl.setFont(font);

		currentGroupLbl = new JLabel();
		currentGroupLbl.setFont(font);

		statusPanel.add(grpLbl, cc.xy(6,1));
		statusPanel.add(currentGroupLbl, cc.xy(7,1));

		final JLabel charLbl = new JLabel(" Character: ");
		charLbl.setFont(font);

		currentCharLbl = new JLabel();
		currentCharLbl.setFont(font);

		statusPanel.add(charLbl, cc.xy(8,1));
		statusPanel.add(currentCharLbl, cc.xy(9,1));
		
		findAndReplacePanel = new FindAndReplacePanel(getEditor());
		findAndReplacePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		findAndReplacePanel.setVisible(false);
		JPanel bottomPanel = new JPanel(new VerticalLayout());
		bottomPanel.add(findAndReplacePanel);
		bottomPanel.add(statusPanel);

		add(bottomPanel, BorderLayout.SOUTH);

		final PhonUIAction<Void> playSegAct = PhonUIAction.runnable(this::playPause);
		playSegAct.putValue(PhonUIAction.NAME, "Play segment");
		playSegAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play current record segment");
		playSegAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		playButton = new JButton(playSegAct);

		crossTierAlignmentView = new CrossTierAlignmentView();
		add(crossTierAlignmentView, BorderLayout.EAST);
		crossTierAlignmentView.setVisible(PrefHelper.getBoolean("phon.debug", false));

		update();
		updateStatus();
		setupEditorActions();
		getEditor().getSelectionModel().addSelectionModelListener(selectionListener);

		getEditor().getMediaModel().getSegmentPlayback().addPropertyChangeListener(segmentPlaybackListener);

		addPropertyChangeListener("fontSizeDelta", e -> {
			PrefHelper.getUserPreferences().putFloat(RecordDataEditorView.FONT_SIZE_DELTA_PROP, getFontSizeDelta());
			update();
			repaint();
		});

	}

	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChange, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordChanged, this::onRecordChange, EditorEventManager.RunOn.AWTEventDispatchThreadInvokeAndWait);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordRefresh, this::onRecordChange, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.SpeakerChanged, this::onSpeakerChange, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.ParticipantAdded, this::onParticipantsChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.ParticipantRemoved, this::onParticipantsChanged, EditorEventManager.RunOn.AWTEventDispatchThread);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordAdded, this::onRecordAdded, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RecordDeleted, this::onRecordDeleted);

		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionLocationChanged, this::onSessionLocationChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
	}

	/**
	 * Upadte the current tier view.
	 *
	 */
	private void update() {
		updating = true;


		Component keyboardFocusedComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		boolean viewFocused =
				(keyboardFocusedComp != null && (this == getEditor().getViewModel().getFocusedView() && SwingUtilities.isDescendingFrom(keyboardFocusedComp, this)));
		final Tier<?> currentFocusTier = currentTier();
		final int grpIdx = currentGroupIndex();

		editorMap.clear();

		contentPane.removeAll();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		if(record == null) return;
		crossTierAlignmentView.setRecord(record);

		final RecordTierEditorListener tierEditorListener = new RecordTierEditorListener(record);

		// update speaker and query exclusion
		recNumField.setText("" + (editor.getCurrentRecordIndex()+1));
		speakerBox.setSelectedItem(record.getSpeaker());
		excludeFromSearchesBox.setSelected(record.isExcludeFromSearches());

		final TierEditorFactory tierEditorFactory = new TierEditorFactory();

		final List<TierViewItem> tierView = session.getTierView();
		int row = 0;

		JComponent toFocus = null;
		for(TierViewItem tierItem:tierView) {
			if(!tierItem.isVisible()) continue;

			final String tierName = tierItem.getTierName();
			
			JPanel tierLabelComp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			tierLabelComp.setOpaque(false);

			DropDownIcon dropDownIcon = new DropDownIcon(new EmptyIcon(0, 16), 0, SwingConstants.BOTTOM);
			
			final JLabel tierLabel = new JLabel(tierName);
			tierLabel.setHorizontalTextPosition(SwingConstants.LEFT);
			tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			tierLabel.setIcon(dropDownIcon);
			tierLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			tierLabelComp.add(tierLabel);

			tierLabel.addMouseListener(new MouseInputAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					showTierContextMenu(e, tierLabel.getText());
				}
			});

			contentPane.add(tierLabelComp, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));

			final SystemTierType systemTier = SystemTierType.tierFromString(tierName);
			TierDescription tierDesc = null;

			final SessionFactory factory = SessionFactory.newFactory();
			if(systemTier != null) {
				tierDesc = factory.createTierDescription(systemTier);
			} else {
				for(TierDescription userTier:session.getUserTiers()) {
					if(userTier.getName().equals(tierName)) {
						tierDesc = userTier;
						break;
					}
				}
			}

			if(tierDesc == null) continue;

			// load tier font
			final String fontString = tierItem.getTierFont();
			Font tierFont = FontPreferences.getTierFont();
			if(fontString != null && !fontString.equalsIgnoreCase("default")) {
				tierFont = Font.decode(fontString);
			}

			float fontSize = getFontSizeDelta() < 0
					? Math.max(FontPreferences.MIN_FONT_SIZE, tierFont.getSize() + getFontSizeDelta())
					: Math.min(FontPreferences.MAX_FONT_SIZE, tierFont.getSize() + getFontSizeDelta());
			tierFont = tierFont.deriveFont(fontSize);

			Tier<?> tier = record.getTier(tierName);
			if(tier == null) {
				tier = factory.createTier(tierDesc.getName(), tierDesc.getDeclaredType(), tierDesc.getTierParameters(), tierDesc.isExcludeFromAlignment());
				record.putTier(tier);
			}
			final TierEditor<?> tierEditor = tierEditorFactory.createTierEditor(getEditor(), tierDesc, record, tier);
			tierEditor.addTierEditorListener(tierEditorListener);
			Component tierComp = tierEditor.getEditorComponent();
			if(tierFont != null)
				tierComp.setFont(tierFont);
			tierComp.addFocusListener(new TierEditorComponentFocusListener(tier, 0));

			if(SystemTierType.Segment.getName().contentEquals(tierName)) {
				JPanel segmentPanel = new JPanel(new GridBagLayout());
				segmentPanel.setOpaque(false);

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;

				segmentPanel.add(tierComp, gbc);

				++gbc.gridx;
				segmentPanel.add(playButton, gbc);

				++gbc.gridx;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				segmentPanel.add(Box.createHorizontalGlue(), gbc);

				tierComp = segmentPanel;
			}

			contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row++));

			if(tierComp instanceof JTextComponent) {
				final JTextComponent textComp = (JTextComponent)tierComp;
				addSelectionHighlights(textComp, editor.getCurrentRecordIndex(), tierName);
				textComp.addCaretListener(caretListener);

				if(tierItem.isTierLocked()) {
					textComp.setEditable(false);
				}
			} else if(tierItem.isTierLocked()) {
				tierComp.setEnabled(false);
			}

			if(currentFocusTier != null && toFocus == null
					&& (currentFocusTier.getName().equals(tier.getName()))) {
				toFocus = tierEditor.getEditorComponent();
			}

			editorMap.put(tierName, tierEditor);
		}

		if(viewFocused && toFocus != null) {
			toFocus.requestFocusInWindow();
		}

		// update location
		updating = false;
		revalidate();
	}

	private void updateStatus() {
		final Record r = getEditor().currentRecord();
		if(r == null) return;

		recordIdLbl.setText(r.getUuid().toString());

		final Tier<?> currentTier = currentTier();
		if(currentTier != null) {
			currentTierLbl.setText(currentTier.getName());

			currentGroupLbl.setText((currentGroupIndex()+1) + " ");

			currentCharLbl.setText(currentCharIndex() + " ");
		}
	}

	public float getFontSizeDelta() {
		return this.fontSizeDelta;
	}

	public void setFontSizeDelta(float fontSizeDelta) {
		float oldVal = this.fontSizeDelta;
		this.fontSizeDelta = fontSizeDelta;
		firePropertyChange("fontSizeDelta", oldVal, fontSizeDelta);
	}

	/*
	 * Session selections
	 */
	private final EditorSelectionModelListener selectionListener =  new EditorSelectionModelListener() {

		@Override
		public void selectionsCleared(EditorSelectionModel model) {
			for(String tierName:editorMap.keySet()) {
				final TierEditor<?> tierEditor = editorMap.get(tierName);
				final JComponent tierComp = tierEditor.getEditorComponent();
				if(tierComp instanceof JTextComponent) {
					// clear highlights
					final Highlighter hl = ((JTextComponent) tierComp).getHighlighter();
					hl.removeAllHighlights();
				}
			}
		}

		@Override
		public void requestSwitchToRecord(EditorSelectionModel model, int recordIndex) {
			getEditor().setCurrentRecordIndex(recordIndex);
		}

		@Override
		public void selectionSet(EditorSelectionModel model,
				SessionEditorSelection selection) {
			selectionsCleared(model);
			selectionAdded(model, selection);
		}

		@Override
		public void selectionAdded(EditorSelectionModel model,
				SessionEditorSelection selection) {
			if(selection.getRecordIndex() == getEditor().getCurrentRecordIndex()) {
				final TierEditor<?> editor = editorMap.get(selection.getTierName());
				final JComponent tierComp = editor.getEditorComponent();
				if(tierComp instanceof JTextComponent) {
					final Highlighter hl = ((JTextComponent) tierComp).getHighlighter();
					final Range r = selection.getRange();
					try {
						HighlightPainter painter = selection.getExtension(HighlightPainter.class);
						if(painter == null)
							painter = new DefaultHighlighter.DefaultHighlightPainter(PhonGuiConstants.PHON_SELECTED);
						hl.addHighlight(r.getFirst(), r.getLast()+1, painter);
					} catch (BadLocationException e) {
						LogUtil.info( e.getLocalizedMessage(), e);
					}
				}
			}
		}

	};

	private void addSelectionHighlights(JTextComponent textComp, int recordIndex, String tierName) {
		final List<SessionEditorSelection> selections =
				getEditor().getSelectionModel().getSelectionsForTier(recordIndex, tierName);
		final Highlighter hl = new GroupFieldHighlighter();
		final Highlighter origHighlighter = textComp.getHighlighter();
		textComp.setHighlighter(hl);
		if(origHighlighter != null && origHighlighter.getHighlights().length > 0) {
			for(Highlight hilight:origHighlighter.getHighlights())
				try {
					hl.addHighlight(hilight.getStartOffset(), hilight.getEndOffset(), hilight.getPainter());
				} catch (BadLocationException e) {
					LogUtil.info( e.getLocalizedMessage(), e);
				}
		}
		for(SessionEditorSelection selection:selections) {
			final Range r = selection.getRange();
			try {
				HighlightPainter painter = selection.getExtension(HighlightPainter.class);
				if(painter == null)
					painter = new DefaultHighlighter.DefaultHighlightPainter(
							OSInfo.isWindows() ? Color.yellow : PhonGuiConstants.PHON_SELECTED);
				hl.addHighlight(r.getFirst(), r.getLast()+1, painter);
			} catch (BadLocationException e) {
				LogUtil.info( e.getLocalizedMessage(), e);
			}
		}
	}

	private JPanel getTopPanel() {
		if(topPanel == null) {
			final TierDataLayout tdl = (TierDataLayout)contentPane.getLayout();
			final TierDataLayoutButtons layoutButtons = new TierDataLayoutButtons(contentPane, tdl);

			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();

			final FormLayout layout = new FormLayout(
					"pref, pref, 3dlu, pref, fill:pref:grow(0.5), 5dlu, pref, fill:pref:grow, right:pref, 5dlu, right:pref, 5dlu, right:pref, 5dlu, right:pref",
					"pref");
			topPanel = new JPanel(layout);

			final JLabel recNumLbl = new JLabel("<html><u>#</u></html>");
			recNumLbl.setForeground(Color.blue);
			recNumLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			recNumLbl.setToolTipText("Click to edit record position (i.e., move record)");
			recNumLbl.addMouseListener(new MouseInputAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					recNumField.setEnabled(true);
					recNumField.requestFocus();
				}

			});

			recNumField = new RecordNumberField(1, getEditor().getSession().getRecordCount());
			recNumField.setColumns(3);
			recNumField.setText("" + (getEditor().getCurrentRecordIndex()+1));
			final PhonUIAction<Void> moveRecordAct = PhonUIAction.runnable(this::moveRecord);
			moveRecordAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit to set this record's position in session");
			recNumField.setAction(moveRecordAct);
			recNumField.addFocusListener(new FocusListener() {

				int initialNum = 0;

				@Override
				public void focusLost(FocusEvent e) {
					if(recNumField.getText().length() == 0) {
						recNumField.setText((getEditor().getCurrentRecordIndex() + 1) + "");
					} else {
						final Integer newNum = Integer.parseInt(recNumField.getText()) - 1;
						if(newNum != initialNum && newNum != getEditor().getCurrentRecordIndex())
							moveRecord();
					}
					recNumField.setEnabled(false);
				}

				@Override
				public void focusGained(FocusEvent e) {
					initialNum = getEditor().getCurrentRecordIndex();
				}

			});
			recNumField.setEnabled(false);

			final CellConstraints cc = new CellConstraints();
			int rowIdx = 1;
			int colIdx = 1;

			topPanel.add(recNumLbl, cc.xy(colIdx++, rowIdx));
			topPanel.add(recNumField, cc.xy(colIdx++, rowIdx));
			colIdx++;  // 3dlu spacer

			final DefaultComboBoxModel<Participant> speakerBoxModel = new DefaultComboBoxModel<>();
			speakerBoxModel.addElement(Participant.UNKNOWN);
			for(Participant participant:session.getParticipants()) {
				speakerBoxModel.addElement(participant);
			}
			speakerBox = new JComboBox<>(speakerBoxModel);
			speakerBox.setRenderer(speakerRenderer);
			speakerBox.addItemListener(speakerListener);

			final PhonUIAction<Void> excludeAct = PhonUIAction.runnable(() -> {});
			excludeAct.putValue(PhonUIAction.NAME, excludeFromSearchesText);
			excludeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Exclude record from queries");
			excludeFromSearchesBox = new JCheckBox(excludeAct);

			topPanel.add(new JLabel("Speaker: "), cc.xy(colIdx++, rowIdx));
			topPanel.add(speakerBox, cc.xy(colIdx++, rowIdx));
			colIdx++; // spacer

			topPanel.add(excludeFromSearchesBox, cc.xy(colIdx++, rowIdx));
			colIdx++; // filler

			// create group management buttons
			final JPanel btnPanel = new JPanel(new HorizontalLayout());

			final PhonUIAction<Void> toggleFindAndReplaceAct = PhonUIAction.eventConsumer(this::onToggleFindAndReplace);
			toggleFindAndReplaceAct.putValue(PhonUIAction.NAME, "");
			toggleFindAndReplaceAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/edit-find-replace", IconSize.SMALL));
			toggleFindAndReplaceAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle Find & Replace panel");
			findAndReplaceButton = new JToggleButton(toggleFindAndReplaceAct);
			findAndReplaceButton.setSelected(false);
			
			topPanel.add(findAndReplaceButton, cc.xy(colIdx++, rowIdx));
			colIdx++;

			fontSizeMenu = new JPopupMenu();
			fontSizeMenu.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					fontSizeMenu.removeAll();

					// setup font scaler
					final JLabel smallLbl = new JLabel("A");
					smallLbl.setFont(getFont().deriveFont(FontPreferences.getDefaultFontSize()));
					smallLbl.setHorizontalAlignment(SwingConstants.CENTER);
					JLabel largeLbl = new JLabel("A");
					largeLbl.setFont(getFont().deriveFont(FontPreferences.getDefaultFontSize()*2));
					largeLbl.setHorizontalAlignment(SwingConstants.CENTER);

					final JSlider scaleSlider = new JSlider(-8, 24);
					scaleSlider.setValue((int)getFontSizeDelta());
					scaleSlider.setMajorTickSpacing(8);
					scaleSlider.setMinorTickSpacing(2);
					scaleSlider.setSnapToTicks(true);
					scaleSlider.setPaintTicks(true);
					scaleSlider.addChangeListener( changeEvent -> {
						int sliderVal = scaleSlider.getValue();
						setFontSizeDelta(sliderVal);
					});

					JComponent fontComp = new JPanel(new HorizontalLayout());
					fontComp.add(smallLbl);
					fontComp.add(scaleSlider);
					fontComp.add(largeLbl);

					fontSizeMenu.add(fontComp);

					fontSizeMenu.addSeparator();

					final PhonUIAction<Float> useDefaultFontSizeAct = PhonUIAction.consumer(RecordDataEditorView.this::setFontSizeDelta, 0.0f);
					useDefaultFontSizeAct.putValue(PhonUIAction.NAME, "Use default font size");
					useDefaultFontSizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset font size");
					fontSizeMenu.add(useDefaultFontSizeAct);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {

				}
			});

			final PhonUIAction<Void> fontSizeAct = PhonUIAction.runnable(() -> {});
			fontSizeAct.putValue(PhonUIAction.NAME, "");
			fontSizeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show font scale menu");
			fontSizeAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL));
			fontSizeAct.putValue(DropDownButton.BUTTON_POPUP, fontSizeMenu);
			fontSizeAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
			fontSizeAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);

			fontSizeButton = new DropDownButton(fontSizeAct);
			fontSizeButton.setOnlyPopup(true);

			topPanel.add(fontSizeButton, cc.xy(colIdx++, rowIdx));
			colIdx++;
			
			topPanel.add(btnPanel, cc.xy(colIdx++, rowIdx));
			colIdx++; // spacer

			topPanel.add(layoutButtons, cc.xy(colIdx++, rowIdx));
		}

		return topPanel;
	}

	private void setupSpeakerBoxModel() {
		if(this.speakerBox == null) return;
		
		Participant selected = (Participant)speakerBox.getModel().getSelectedItem();
		final DefaultComboBoxModel<Participant> speakerBoxModel = new DefaultComboBoxModel<>();
		speakerBoxModel.addElement(Participant.UNKNOWN);
		for(Participant participant:getEditor().getSession().getParticipants()) {
			speakerBoxModel.addElement(participant);
		}
		speakerBox.setModel(speakerBoxModel);
		if(selected != null)
			speakerBox.setSelectedItem(selected);
	}
	
	private TierEdit<PhoneAlignment> updateRecordAlignment(Record record) {
		final Tier<IPATranscript> ipaTarget = record.getIPATargetTier();
		final Tier<IPATranscript> ipaActual = record.getIPAActualTier();
		final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(ipaTarget, ipaActual);
		final TierEdit<PhoneAlignment> pmEdit = new TierEdit<>(getEditor(), record.getPhoneAlignmentTier(), phoneAlignment);
		return pmEdit;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}

	private class RecordTierEditorListener<T> implements TierEditorListener<T> {

		private final Record record;

		public RecordTierEditorListener(Record record) {
			this.record = record;
		}

		@Override
		public void tierValueChanged(Tier<T> tier, T newValue, T oldValue, boolean valueIsAdjusting) {
			if(valueIsAdjusting) {
				final TierEdit<T> tierEdit = new TierEdit<T>(getEditor(), tier, newValue);
				getEditor().getUndoSupport().postEdit(tierEdit);
			} else {
				final EditorEvent<EditorEventType.TierChangeData> ee = new EditorEvent<>(EditorEventType.TierChange, RecordDataEditorView.this,
						new EditorEventType.TierChangeData(tier.isBlind() ? getEditor().getDataModel().getTranscriber() : Transcriber.VALIDATOR,
								getEditor().currentRecord(), tier, oldValue, newValue, false));
				getEditor().getEventManager().queueEvent(ee);
			}
		}
	};

	private final DefaultListCellRenderer speakerRenderer = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if(value != null) {
				final Participant participant = (Participant)value;
				final String val =
						(participant.getName() != null && participant.getName().length() > 0 ? participant.getName() :
							participant.getId() != null ? participant.getId() : participant.getRole().toString());
				retVal.setText(val);
			} else {
				retVal.setText("Unspecified");
			}
			return retVal;
		}

	};

	private final ItemListener speakerListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(!updating) {
				final Object selectedObj = speakerBox.getSelectedItem();
				final SessionEditor editor = getEditor();
				final Record record = editor.currentRecord();
				final Participant selectedSpeaker = Participant.class.cast(selectedObj);
				final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(editor, record, selectedSpeaker);
				editor.getUndoSupport().postEdit(edit);
			}
		}

	};
	
	public void playPause() {
		SessionEditor editor = getEditor();
		if(editor == null) return;

		if(isPlaying()) {
			stopPlaying();
		} else {
			playSegment();
		}
	}
	
	public boolean isPlaying() {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		return mediaModel.getSegmentPlayback().isPlaying();
	}
	
	public void stopPlaying() {
		SessionMediaModel mediaModel = getEditor().getMediaModel();
		mediaModel.getSegmentPlayback().stopPlaying();
	}
	
	public void playSegment() {
		PlaySegmentAction playSegAct = new PlaySegmentAction(getEditor());
		playSegAct.actionPerformed(new ActionEvent(this, -1, "play_segment"));
	}

	/**
	 * Return the record of the last tier that was focused.
	 *
	 * @return record index
	 */
	public int currentRecordIndex() {
		return (currentRecordIndex != null ? currentRecordIndex.get() : -1);
	}

	/**
	 * Return the 'current' tier.  This is the last tier that
	 * was focused within the editor.
	 *
	 * @return current tier or <code>null</code> if not
	 *  set
	 */
	public Tier<?> currentTier() {
		return (currentTierRef != null ? currentTierRef.get() : null);
	}

	/**
	 * Return the current group index.
	 *
	 * @return the current group index, < 0 if not set
	 */
	public int currentGroupIndex() {
		return (currentGroupIndex != null ? currentGroupIndex.get() : -1);
	}

	public int currentWordIndex() {
		int retVal = -1;

		final JComponent lastComp = lastFocusedRef.get();
		if(lastComp != null && lastComp instanceof JTextComponent) {
			final JTextComponent textComp = (JTextComponent)lastComp;

			final String text = textComp.getText();
			final int caretIdx = textComp.getCaretPosition();
			retVal = 0;
			for(int i = 0; i < caretIdx; i++) {
				if(text.charAt(i) == ' ')
					retVal++;
			}
			if(caretIdx == text.length() || text.charAt(caretIdx) == ' ')
				retVal++;
		}

		return retVal;
	}

	public int currentCharIndex() {
		return currentCharIndex.get();
	}

	public SessionLocation getSessionLocation() {
		if(currentTierRef.get() == null) return null;
		final RecordLocation recLoc = new RecordLocation(currentTier().getName(), currentCharIndex());
		final SessionLocation sessionLoc = new SessionLocation(currentRecordIndex(), recLoc);
		return sessionLoc;
	}

	public void fireSessionLocationChanged() {
		final EditorEvent<SessionLocation> ee =
				new EditorEvent(EditorEventType.SessionLocationChanged, this, getSessionLocation());
		getEditor().getEventManager().queueEvent(ee);
	}

	/**
	 * Show context menu for given tier
	 *
	 * @param me
	 * @param tierName
	 */
	public void showTierContextMenu(MouseEvent me, String tierName) {
		JPopupMenu tierMenu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(tierMenu);

		Tier<?> tier = getEditor().currentRecord().getTier(tierName);

		final PhonUIAction<Tier<?>> copyTierAct = PhonUIAction.eventConsumer(this::onCopyTier, tier);
		copyTierAct.putValue(PhonUIAction.NAME, "Copy tier");
		copyTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Copy tier contents to clipboard");
		builder.addItem(".", copyTierAct);

		if(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(TierTransferrable.FLAVOR)
			|| Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			final PhonUIAction<Tuple<Record, Tier<?>>> pasteTierAct =
					PhonUIAction.eventConsumer(this::onPasteTier, new Tuple<Record, Tier<?>>(getEditor().currentRecord(), tier));
			pasteTierAct.putValue(PhonUIAction.NAME, "Paste tier");
			pasteTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Paste tier data");
			builder.addItem(".", pasteTierAct);
		}
		builder.addSeparator(".", "copy_paste");

		TierOrderingEditorView tierOrderView =
				(TierOrderingEditorView) getEditor().getViewModel().getView(TierOrderingEditorView.VIEW_NAME);
		if(tierOrderView != null) {
			for(int i = 0; i < getEditor().getSession().getTierView().size(); i++) {
				TierViewItem tvi = getEditor().getSession().getTierView().get(i);
				if(tvi.getTierName().equals(tierName)) {
					tierOrderView.setupTierContextMenu(i, builder);
					break;
				}
			}
		}

		tierMenu.show(me.getComponent(), 0, me.getComponent().getHeight());
	}

	public void onCopyTier(PhonActionEvent<Tier<?>> pae) {
		Tier<?> tier = pae.getData();

		TierTransferrable tierTrans = new TierTransferrable(tier);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tierTrans, this);
	}

	private final static String GROUPED_TIER_PATTERN = "\\[.*?\\](\\p{Space}?\\[.*?\\])*";
	private final static String GROUP_DATA_PATTERN = "\\[(.*?)\\]";
	public void onPasteTier(PhonActionEvent<Tuple<Record, Tier<?>>> pae) {
		Tuple<Record, Tier<?>> tuple = pae.getData();
		Record destRecord = tuple.getObj1();
		Tier<?> destTier = tuple.getObj2();

		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
		if(transferable.isDataFlavorSupported(TierTransferrable.FLAVOR)) {
			try {
				TierTransferrable tierTransferrable = (TierTransferrable) transferable.getTransferData(TierTransferrable.FLAVOR);
				pasteTier(destRecord, destTier, tierTransferrable.getTier().toString());
			} catch (IOException | UnsupportedFlavorException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.warning(e);
			}
		} else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String clipboardText = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
				pasteTier(destRecord, destTier, clipboardText);
			} catch (IOException | UnsupportedFlavorException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
		} else {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.warning("Pasting from other data flavors not supported");
		}
	}

	private void pasteTier(Record destRecord, Tier<?> destTier, String text) {
		getEditor().getUndoSupport().beginUpdate();
		final TierEdit<?> tierEdit = new TierEdit<>(getEditor(), destTier, text);
		getEditor().getUndoSupport().postEdit(tierEdit);
		if(destTier.getDeclaredType() == IPATranscript.class) {
			updateIPATier(destRecord, (Tier<IPATranscript>) destTier);
		}
		getEditor().getUndoSupport().endUpdate();
	}

	private void updateIPATier(Record record, Tier<IPATranscript> ipaTier) {
		final IPATranscript ipa = ipaTier.getValue();
		SyllabifierInfo info = getEditor().getSession().getExtension(SyllabifierInfo.class);
		SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		Syllabifier syllabifier = library.defaultSyllabifier();
		if (info != null) {
			Syllabifier tierSyllabifier = library.getSyllabifierForLanguage(info.getSyllabifierLanguageForTier(ipaTier.getName()));
			if (tierSyllabifier != null) {
				syllabifier = tierSyllabifier;
			}
		}
		syllabifier.syllabify(ipa.toList());
		final SystemTierType systemTier = SystemTierType.tierFromString(ipaTier.getName());
		if(systemTier == SystemTierType.IPATarget || systemTier == SystemTierType.IPAActual) {
			final Tier<IPATranscript> targetTier =
					systemTier == SystemTierType.IPATarget ? ipaTier : record.getIPATargetTier();
			final Tier<IPATranscript> actualTier =
					systemTier == SystemTierType.IPAActual ? ipaTier : record.getIPAActualTier();
			final PhoneAlignment phoneAlignment = PhoneAlignment.fromTiers(targetTier, actualTier);
			final TierEdit<PhoneAlignment> alignmentTierEdit = new TierEdit<>(getEditor(), record.getPhoneAlignmentTier(), phoneAlignment);
			getEditor().getUndoSupport().postEdit(alignmentTierEdit);
		}
	}

	/*
	 * Editor Actions
	 */
	private void onSessionChanged(EditorEvent<Session> ee) {
		update();
		updateStatus();
		repaint();
	}

	private void onRecordAdded(EditorEvent<EditorEventType.RecordAddedData> ee) {
		onNumRecordsChanged(ee);
	}

	private void onRecordDeleted(EditorEvent<EditorEventType.RecordDeletedData> ee) {
		onNumRecordsChanged(ee);
	}

	private void onNumRecordsChanged(EditorEvent<?> event) {
		recNumField.setMaxNumber(getEditor().getSession().getRecordCount());
	}

	private void onTierViewChange(EditorEvent<EditorEventType.TierViewChangedData> event) {
		update();
		repaint();
	}

	private void onRecordChange(EditorEvent<EditorEventType.RecordChangedData> event) {
		update();
		updateStatus();
		repaint();
	}

	private void onSpeakerChange(EditorEvent<EditorEventType.SpeakerChangedData> event) {
		updating = true;
		speakerBox.setSelectedItem(event.data().newSpeaker());
		updating = false;
	}
	
	public void onParticipantsChanged(EditorEvent<Participant> event) {
		updating = true;
		setupSpeakerBoxModel();
		updating = false;
	}

	private void onSessionLocationChanged(EditorEvent<SessionLocation> event) {
		updateStatus();
	}

	public void moveRecord() {
		final Integer newRecordNumber = Integer.parseInt(recNumField.getText())-1;

		if(newRecordNumber >= 0 && newRecordNumber < getEditor().getSession().getRecordCount()) {
			final MoveRecordEdit edit = new MoveRecordEdit(getEditor(), getEditor().currentRecord(), newRecordNumber);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
	/* Find & Replace */
	public boolean isFindAndReplaceVisible() {
		return this.findAndReplacePanel.isVisible();
	}
	
	public void setFindAndReplaceVisible(boolean visible) {
		this.findAndReplacePanel.setVisible(visible);
		this.findAndReplaceButton.setSelected(visible);
	}
	
	public void onToggleFindAndReplace(PhonActionEvent<Void> pae) {
		setFindAndReplaceVisible(!isFindAndReplaceVisible());
	}
	
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
		return new RecordDataMenu(this);
	}
	
	private PropertyChangeListener segmentPlaybackListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			SegmentPlayback segPlayback = (SegmentPlayback)evt.getSource();
			if(SegmentPlayback.PLAYBACK_PROP.equals(evt.getPropertyName())) {
				if(segPlayback.isPlaying()) {
					playButton.setText("Stop playback");
					playButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
				} else {
					playButton.setText("Play segment");
					playButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
				}
			}
 		}
		
	};

	private AtomicReference<JComponent> lastFocusedRef = new AtomicReference<JComponent>();
	private final class TierEditorComponentFocusListener implements FocusListener {

		private final Tier<?> tier;

		private final int group;

		public TierEditorComponentFocusListener(Tier<?> tier, int group) {
			super();
			this.tier = tier;
			this.group = group;
		}

		@Override
		public void focusGained(FocusEvent e) {
			final JComponent tierComp = (JComponent)e.getComponent();
			lastFocusedRef.getAndSet(tierComp);

			currentRecordIndex.getAndSet(getEditor().getCurrentRecordIndex());
			currentTierRef.getAndSet(tier);
			currentGroupIndex.getAndSet(group);

			if(tierComp instanceof JTextComponent) {
				final JTextComponent textComp = (JTextComponent)tierComp;
				currentCharIndex.getAndSet(textComp.getCaretPosition());
			} else {
				currentCharIndex.getAndSet(0);
			}

			fireSessionLocationChanged();
		}

		@Override
		public void focusLost(FocusEvent e) {
		}

	}

	private final CaretListener caretListener = new CaretListener() {

		@Override
		public void caretUpdate(CaretEvent e) {
			if(((JComponent)e.getSource()).hasFocus()) {
				if(e.getDot() >= 0) {
					currentCharIndex.getAndSet(e.getDot());
					fireSessionLocationChanged();
				}
			}
		}

	};

}

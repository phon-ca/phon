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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Highlighter.*;
import javax.swing.undo.*;

import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.ui.menu.MenuBuilder;
import org.jdesktop.swingx.*;

import com.jgoodies.forms.layout.*;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.app.session.editor.view.record_data.actions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.position.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * Editor view for tier data.
 *
 */
public class RecordDataEditorView extends EditorView {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(RecordDataEditorView.class.getName());

	private static final long serialVersionUID = 2961561720211049250L;

	public final static String VIEW_NAME = "Record Data";

	public final static String VIEW_ICON = "misc/record";

	private JPanel topPanel;

	/**
	 * speaker selection
	 */
	private JComboBox<Participant> speakerBox;
	

	private volatile boolean updating = false;

	/**
	 * query exclusion
	 */
	private JCheckBox excludeFromSearchesBox;
	private final static String excludeFromSearchesText = "Exclude from searches";

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
	private JButton findAndReplaceButton;
	private FindAndReplacePanel findAndReplacePanel;

	/*
	 * Keep track of 'current' group and tier.  This is done by tracking
	 * focus of the various TierEditors.
	 */
	private final AtomicReference<Tier<?>> currentTierRef = new AtomicReference<Tier<?>>();

	private final AtomicInteger currentGroupIndex = new AtomicInteger(-1);

	private final AtomicInteger currentRecordIndex = new AtomicInteger(-1);

	private final AtomicInteger currentCharIndex = new AtomicInteger(-1);

	private final Map<String, List<TierEditor>> editorMap = new LinkedHashMap<>();

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

		final PhonUIAction playSegAct = new PhonUIAction(this, "playPause");
		playSegAct.putValue(PhonUIAction.NAME, "Play segment");
		playSegAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Play current record segment");
		playSegAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		playButton = new JButton(playSegAct);
		
		update();
		updateStatus();
		setupEditorActions();
		getEditor().getSelectionModel().addSelectionModelListener(selectionListener);
		
		getEditor().getMediaModel().getSegmentPlayback().addPropertyChangeListener(segmentPlaybackListener);
	}

	private void setupEditorActions() {
		final EditorAction onTierViewChangeAct =
				new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, onTierViewChangeAct);

		final EditorAction onRecordChangeAct =
				new DelegateEditorAction(this, "onRecordChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_REFRESH_EVT, onRecordChangeAct);

		final EditorAction onSpeakerChangeAct =
				new DelegateEditorAction(this, "onSpeakerChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SPEAKER_CHANGE_EVT, onSpeakerChangeAct);
		
		final EditorAction onGroupListChangeAct =
				new DelegateEditorAction(this, "onGroupsChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, onGroupListChangeAct);

		final EditorAction onParticipantsChangedAct =
				new DelegateEditorAction(this, "onParticipantsChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantsChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantsChangedAct);

		final EditorAction onNumRecordsChangeAct =
				new DelegateEditorAction(this, "onNumRecordsChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, onNumRecordsChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, onNumRecordsChangeAct);

		final EditorAction onSessionLocationChangedAct =
				new DelegateEditorAction(this, "onSessionLocationChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SESSION_LOCATION_CHANGED_EVT, onSessionLocationChangedAct);
	}

	/**
	 * Upadte the current tier view.
	 *
	 */
	private void update() {
		updating = true;

		editorMap.clear();

		contentPane.removeAll();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		if(record == null) return;

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
				tierDesc = factory.createTierDescription(tierName, systemTier.isGrouped());
			} else {
				for(TierDescription userTier:session.getUserTiers()) {
					if(userTier.getName().equals(tierName)) {
						tierDesc = userTier;
						break;
					}
				}
			}

			if(tierDesc == null) continue;

			boolean isGrouped = tierDesc.isGrouped();

			// load tier font
			final String fontString = tierItem.getTierFont();
			Font tierFont = FontPreferences.getTierFont();
			if(fontString != null && !fontString.equalsIgnoreCase("default")) {
				tierFont = Font.decode(fontString);
			}

			Tier<?> tier = record.getTier(tierName);
			if(tier == null) {
				tier = factory.createTier(tierDesc.getName(), tierDesc.getDeclaredType(), isGrouped);
				record.putTier(tier);
			}
			if(isGrouped) {
				List<TierEditor> editors = new ArrayList<>();
				editorMap.put(tier.getName(), editors);
				for(int gIdx = 0; gIdx < record.numberOfGroups(); gIdx++) {
					final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tierDesc, tier, gIdx);
					tierEditor.addTierEditorListener(tierEditorListener);
					final Component tierComp = tierEditor.getEditorComponent();
					if(tierFont != null)
						tierComp.setFont(tierFont);
					tierComp.addFocusListener(new TierEditorComponentFocusListener(tier, gIdx));
					contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIdx, row));

					if(tierComp instanceof JTextComponent) {
						final JTextComponent textComp = (JTextComponent)tierComp;
						addSelectionHighlights(textComp, editor.getCurrentRecordIndex(), tierName, gIdx);
						textComp.addCaretListener(caretListener);

						if(tierItem.isTierLocked()) {
							textComp.setEditable(false);
						}
					} else if(tierItem.isTierLocked()) {
						tierComp.setEnabled(false);
					}

					if(toFocus == null) {
						toFocus = (JComponent)tierComp;
					}
					editors.add(tierEditor);
				}
			} else {
				final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tierDesc, tier, 0);
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
				
				contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row));

				if(tierComp instanceof JTextComponent) {
					final JTextComponent textComp = (JTextComponent)tierComp;
					addSelectionHighlights(textComp, editor.getCurrentRecordIndex(), tierName, 0);
					textComp.addCaretListener(caretListener);

					if(tierItem.isTierLocked()) {
						textComp.setEditable(false);
					}
				} else if(tierItem.isTierLocked()) {
					tierComp.setEnabled(false);
				}

				if(toFocus == null) {
					toFocus = (JComponent)tierComp;
				}
				editorMap.put(tierName, Collections.singletonList(tierEditor));
			}
			row++;
		}

		if(isFocusOwner() && toFocus != null) {
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

	/*
	 * Session selections
	 */
	private final EditorSelectionModelListener selectionListener =  new EditorSelectionModelListener() {

		@Override
		public void selectionsCleared(EditorSelectionModel model) {
			for(String tierName:editorMap.keySet()) {
				final List<TierEditor> editors = editorMap.get(tierName);
				for(TierEditor editor:editors) {
					final JComponent tierComp = editor.getEditorComponent();
					if(tierComp instanceof JTextComponent) {
						// clear highlights
						final Highlighter hl = ((JTextComponent) tierComp).getHighlighter();
						hl.removeAllHighlights();
					}
				}
			}
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
				final List<TierEditor> editors = editorMap.get(selection.getTierName());
				if(editors != null && editors.size() > selection.getGroupIndex()) {
					final TierEditor editor = editors.get(selection.getGroupIndex());
					final JComponent tierComp = editor.getEditorComponent();
					if(tierComp instanceof JTextComponent) {
						final Highlighter hl = ((JTextComponent) tierComp).getHighlighter();
						final Range r = selection.getGroupRange();
						try {
							HighlightPainter painter = selection.getExtension(HighlightPainter.class);
							if(painter == null)
								painter = new DefaultHighlighter.DefaultHighlightPainter(PhonGuiConstants.PHON_SELECTED);
							hl.addHighlight(r.getFirst(), r.getLast()+1, painter);
						} catch (BadLocationException e) {
							LOGGER.info( e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}

	};

	private void addSelectionHighlights(JTextComponent textComp, int recordIndex, String tierName, int groupIndex) {
		final List<SessionEditorSelection> selections =
				getEditor().getSelectionModel().getSelectionsForGroup(recordIndex,
						tierName, groupIndex);
		final Highlighter hl = new GroupFieldHighlighter();
		final Highlighter origHighlighter = textComp.getHighlighter();
		textComp.setHighlighter(hl);
		if(origHighlighter != null && origHighlighter.getHighlights().length > 0) {
			for(Highlight hilight:origHighlighter.getHighlights())
				try {
					hl.addHighlight(hilight.getStartOffset(), hilight.getEndOffset(), hilight.getPainter());
				} catch (BadLocationException e) {
					LOGGER.info( e.getLocalizedMessage(), e);
				}
		}
		for(SessionEditorSelection selection:selections) {
			final Range r = selection.getGroupRange();
			try {
				HighlightPainter painter = selection.getExtension(HighlightPainter.class);
				if(painter == null)
					painter = new DefaultHighlighter.DefaultHighlightPainter(PhonGuiConstants.PHON_SELECTED);
				hl.addHighlight(r.getFirst(), r.getLast()+1, painter);
			} catch (BadLocationException e) {
				LOGGER.info( e.getLocalizedMessage(), e);
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
					"pref, pref, 3dlu, pref, fill:pref:grow(0.5), 5dlu, pref, fill:pref:grow, right:pref, 5dlu, right:pref, 5dlu, right:pref",
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
			final PhonUIAction moveRecordAct = new PhonUIAction(this, "moveRecord");
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

			final PhonUIAction excludeAct = new PhonUIAction(this, "onExclude");
			excludeAct.putValue(PhonUIAction.NAME, excludeFromSearchesText);
			excludeFromSearchesBox = new JCheckBox(excludeAct);

			topPanel.add(new JLabel("Speaker: "), cc.xy(colIdx++, rowIdx));
			topPanel.add(speakerBox, cc.xy(colIdx++, rowIdx));
			colIdx++; // spacer

			topPanel.add(excludeFromSearchesBox, cc.xy(colIdx++, rowIdx));
			colIdx++; // filler

			// create group management buttons
			final JPanel btnPanel = new JPanel(new HorizontalLayout());

			final NewGroupCommand newGroupAct = new NewGroupCommand(this);
			newGroupAct.putValue(NewGroupCommand.SHORT_DESCRIPTION, newGroupAct.getValue(NewGroupCommand.NAME));
			newGroupAct.putValue(NewGroupCommand.NAME, null);
			final JButton newGroupBtn = new JButton(newGroupAct);
			newGroupBtn.setFocusable(false);
			btnPanel.add(newGroupBtn);

			final MergeGroupCommand mergeGroupAct = new MergeGroupCommand(this);
			mergeGroupAct.putValue(MergeGroupCommand.SHORT_DESCRIPTION, mergeGroupAct.getValue(MergeGroupCommand.NAME));
			mergeGroupAct.putValue(MergeGroupCommand.NAME, null);
			final JButton mergeGroupBtn = new JButton(mergeGroupAct);
			mergeGroupBtn.setFocusable(false);
			btnPanel.add(mergeGroupBtn);

			final SplitGroupCommand splitGroupAct = new SplitGroupCommand(this);
			splitGroupAct.putValue(SplitGroupCommand.SHORT_DESCRIPTION, splitGroupAct.getValue(SplitGroupCommand.NAME));
			splitGroupAct.putValue(SplitGroupCommand.NAME, null);
			final JButton splitGroupBtn = new JButton(splitGroupAct);
			splitGroupBtn.setFocusable(false);
			btnPanel.add(splitGroupBtn);
			
			final PhonUIAction toggleFindAndReplaceAct = new PhonUIAction(this, "onToggleFindAndReplace");
			toggleFindAndReplaceAct.putValue(PhonUIAction.NAME, "");
			toggleFindAndReplaceAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/edit-find-replace", IconSize.SMALL));
			toggleFindAndReplaceAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle Find & Replace");
			findAndReplaceButton = new JButton(toggleFindAndReplaceAct);
			
			topPanel.add(findAndReplaceButton, cc.xy(colIdx++, rowIdx));
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
	
	private SessionEditorUndoableEdit updateRecordAlignment(Record record, int group) {
		final Tier<IPATranscript> ipaTarget = record.getIPATarget();
		final Tier<IPATranscript> ipaActual = record.getIPAActual();

		final IPATranscript targetGroup = (group < ipaTarget.numberOfGroups() ? ipaTarget.getGroup(group) : new IPATranscript());
		final IPATranscript actualGroup = (group < ipaActual.numberOfGroups() ? ipaActual.getGroup(group) : new IPATranscript());
		final PhoneAligner aligner = new PhoneAligner();
		final PhoneMap pm = aligner.calculatePhoneMap(targetGroup, actualGroup);

		final TierEdit<PhoneMap> pmEdit = new TierEdit<PhoneMap>(getEditor(), record.getPhoneAlignment(), group, pm);

		return pmEdit;
	}

	private class RecordTierEditorListener implements TierEditorListener {

		private final Record record;

		public RecordTierEditorListener(Record record) {
			this.record = record;
		}

		@Override
		public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> tierEdit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			tierEdit.setFireHardChangeOnUndo(true);

			UndoableEdit edit = tierEdit;

			// XXX
			// Special case for IPA tiers, alignment must be updated as well
			if(SystemTierType.IPATarget.getName().equals(tier.getName()) ||
					SystemTierType.IPAActual.getName().equals(tier.getName())) {
				edit = new CompoundEdit();
				tierEdit.doIt();
				edit.addEdit(tierEdit);

				final SessionEditorUndoableEdit alignEdit = updateRecordAlignment(record, groupIndex);
				alignEdit.doIt();
				edit.addEdit(alignEdit);

				((CompoundEdit)edit).end();

				// we also need to send out a TIER_DATA_CHANGED event so the syllabification/alignment view updates
				final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, this, SystemTierType.SyllableAlignment.getName());
				getEditor().getEventManager().queueEvent(ee);
			}

			getEditor().getUndoSupport().postEdit(edit);
		}

		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex,
				T newValue, T oldValue) {
			final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, RecordDataEditorView.this, tier.getName());
			getEditor().getEventManager().queueEvent(ee);
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

	public void onExclude() {
		final boolean exclude = excludeFromSearchesBox.isSelected();
		final RecordExcludeEdit edit = new RecordExcludeEdit(getEditor(), getEditor().currentRecord(), exclude);
		getEditor().getUndoSupport().postEdit(edit);
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
		final GroupLocation grpLoc = new GroupLocation(currentGroupIndex(), currentCharIndex());
		final RecordLocation recLoc = new RecordLocation(currentTier().getName(), grpLoc);
		final SessionLocation sessionLoc = new SessionLocation(currentRecordIndex(), recLoc);
		return sessionLoc;
	}

	public void fireSessionLocationChanged() {
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LOCATION_CHANGED_EVT, this,
				getSessionLocation());
		getEditor().getEventManager().queueEvent(ee);
	}

	/**
	 * Show context menu for given tier
	 *
	 * @param tier
	 */
	public void showTierContextMenu(MouseEvent me, String tier) {
		JPopupMenu tierMenu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(tierMenu);

		TierOrderingEditorView tierOrderView =
				(TierOrderingEditorView) getEditor().getViewModel().getView(TierOrderingEditorView.VIEW_TITLE);
		if(tierOrderView != null) {
			for(int i = 0; i < getEditor().getSession().getTierView().size(); i++) {
				TierViewItem tvi = getEditor().getSession().getTierView().get(i);
				if(tvi.getTierName().equals(tier)) {
					tierOrderView.setupTierContextMenu(i, builder);
					break;
				}
			}
		}

		tierMenu.show(me.getComponent(), 0, me.getComponent().getHeight());
	}

	/*
	 * Editor Actions
	 */
	@RunOnEDT
	public void onNumRecordsChanged(EditorEvent event) {
		recNumField.setMaxNumber(getEditor().getSession().getRecordCount());
	}

	@RunOnEDT
	public void onTierViewChange(EditorEvent event) {
		update();
		repaint();
	}

	@RunOnEDT
	public void onRecordChange(EditorEvent event) {
		update();
		updateStatus();
		repaint();
	}

	@RunOnEDT
	public void onSpeakerChange(EditorEvent event) {
		updating = true;
		
		speakerBox.setSelectedItem(((Record)event.getEventData()).getSpeaker());
		
		updating = false;
	}
	
	@RunOnEDT
	public void onGroupsChange(EditorEvent event) {
		final SessionLocation location = getSessionLocation();
		if(location != null &&
				location.getRecordLocation().getGroupLocation().getGroupIndex() >= getEditor().currentRecord().numberOfGroups()) {
			currentGroupIndex.set(getEditor().currentRecord().numberOfGroups()-1);
			currentCharIndex.set(0);
			fireSessionLocationChanged();
		}
		update();
		repaint();
	}

	@RunOnEDT
	public void onParticipantsChanged(EditorEvent event) {
		setupSpeakerBoxModel();
	}

	@RunOnEDT
	public void onSessionLocationChanged(EditorEvent event) {
		updateStatus();
	}

	public void moveRecord() {
		final Integer newRecordNumber = Integer.parseInt(recNumField.getText())-1;

		if(newRecordNumber >= 0 && newRecordNumber < getEditor().getSession().getRecordCount()) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), getEditor().currentRecord(), newRecordNumber);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}
	
	/* Find & Replace */
	public boolean isFindAndReplaceVisible() {
		return this.findAndReplacePanel.isVisible();
	}
	
	public void setFindAndReplaceVisible(boolean visible) {
		this.findAndReplacePanel.setVisible(visible);
	}
	
	public void onToggleFindAndReplace(PhonActionEvent pae) {
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
//			final JComponent comp = (JComponent)e.getSource();
//			if(comp != null && comp instanceof JTextComponent) {
//				final JTextComponent textComp = (JTextComponent)comp;
//				if(textComp.getSelectedText() != null) {
//					textComp.setSelectionStart(-1);
//					textComp.setSelectionEnd(-1);
//				}
//			}
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

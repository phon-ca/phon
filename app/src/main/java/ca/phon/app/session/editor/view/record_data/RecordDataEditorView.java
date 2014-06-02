package ca.phon.app.session.editor.view.record_data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorSelection;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.RecordExcludeEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.GroupFieldHighlighter;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayout;
import ca.phon.app.session.editor.view.common.TierDataLayoutButtons;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.common.TierEditor;
import ca.phon.app.session.editor.view.common.TierEditorFactory;
import ca.phon.app.session.editor.view.common.TierEditorListener;
import ca.phon.app.session.editor.view.record_data.actions.DeleteGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.MergeGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.NewGroupCommand;
import ca.phon.app.session.editor.view.record_data.actions.SplitGroupCommand;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.GroupLocation;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.RecordLocation;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionLocation;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.util.Range;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Editor view for tier data.
 *
 */
public class RecordDataEditorView extends EditorView {
	
	private final static Logger LOGGER = Logger.getLogger(RecordDataEditorView.class.getName());

	private static final long serialVersionUID = 2961561720211049250L;
	
	public final static String VIEW_NAME = "Record Data";
	
	public final static String VIEW_ICON = "misc/record";
	
	/*
	 * Common data for all records
	 */
	private JPanel topPanel;

	/**
	 * speaker selection
	 */
	private JComboBox speakerBox;
	
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
	
	/*
	 * Keep track of 'current' group and tier.  This is done by tracking
	 * focus of the various TierEditors.
	 */
	private final AtomicReference<Tier<?>> currentTierRef = new AtomicReference<Tier<?>>();
	
	private final AtomicInteger currentGroupIndex = new AtomicInteger(-1);
	
	private final AtomicInteger currentRecordIndex = new AtomicInteger(-1);
	
	private final AtomicInteger currentCharIndex = new AtomicInteger(-1);
	
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
		
		add(statusPanel, BorderLayout.SOUTH);
		
		update();
		updateStatus();
		setupEditorActions();
	}
	
	private void setupEditorActions() {
		final EditorAction onTierViewChangeAct = 
				new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, onTierViewChangeAct);
	
		final EditorAction onRecordChangeAct =
				new DelegateEditorAction(this, "onRecordChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_REFRESH_EVT, onRecordChangeAct);
		
		final EditorAction onGroupListChangeAct =
				new DelegateEditorAction(this, "onGroupsChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.GROUP_LIST_CHANGE_EVT, onGroupListChangeAct);
		
		final EditorAction onParticipantsChangedAct =
				new DelegateEditorAction(this, "onParticipantsChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantsChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantsChangedAct);

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
		contentPane.removeAll();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.getDataModel().getRecord(editor.getCurrentRecordIndex());

		// update speaker and query exclusion
		speakerBox.setSelectedItem(record.getSpeaker());
		excludeFromSearchesBox.setSelected(record.isExcludeFromSearches());
		
		final TierEditorFactory tierEditorFactory = new TierEditorFactory();
		
		final List<TierViewItem> tierView = session.getTierView();
		int row = 0;
		
		JComponent toFocus = null;
		for(TierViewItem tierItem:tierView) {
			if(!tierItem.isVisible()) continue;
			
			final String tierName = tierItem.getTierName();
			final JLabel tierLabel = new JLabel(tierName);
			tierLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
			tierLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			
			contentPane.add(tierLabel, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
			
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
			Font tierFont = PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
					Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
			if(fontString != null && !fontString.equalsIgnoreCase("default")) {
				tierFont = Font.decode(fontString);
			}
			
			Tier<?> tier = record.getTier(tierName);
			if(tier == null) {
				tier = factory.createTier(tierDesc.getName(), tierDesc.getDeclaredType(), isGrouped);
				record.putTier(tier);
			}
			if(isGrouped) {
				for(int gIdx = 0; gIdx < record.numberOfGroups(); gIdx++) {
					final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tierDesc, tier, gIdx);
					tierEditor.addTierEditorListener(tierEditorListener);
					final Component tierComp = tierEditor.getEditorComponent();
					tierComp.setFont(tierFont);
					tierComp.addFocusListener(new TierEditorComponentFocusListener(tier, gIdx));
					contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIdx, row));
					
					if(tierComp instanceof JTextComponent) {
						final JTextComponent textComp = (JTextComponent)tierComp;
						addSelectionHighlights(textComp, editor.getCurrentRecordIndex(), tierName, gIdx);
						textComp.addCaretListener(caretListener);
					}
					
					if(toFocus == null) {
						toFocus = (JComponent)tierComp;
					}
				}
			} else {
				final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tierDesc, tier, 0);

				tierEditor.addTierEditorListener(tierEditorListener);
				final Component tierComp = tierEditor.getEditorComponent();
				tierComp.setFont(tierFont);
				tierComp.addFocusListener(new TierEditorComponentFocusListener(tier, 0));
				contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row));
				
				if(tierComp instanceof JTextComponent) {
					final JTextComponent textComp = (JTextComponent)tierComp;
					addSelectionHighlights(textComp, editor.getCurrentRecordIndex(), tierName, 0);
					textComp.addCaretListener(caretListener);
				}
				
				if(toFocus == null) {
					toFocus = (JComponent)tierComp;
				}
			}
			row++;
		}
		
		if(hasFocus() && toFocus != null) {
			toFocus.requestFocusInWindow();
		}
		
		updating = false;
		revalidate();
	}
	
	private void updateStatus() {
		final Record r = getEditor().currentRecord();
		recordIdLbl.setText(r.getUuid().toString());
		
		final Tier<?> currentTier = currentTier();
		if(currentTier != null) {
			currentTierLbl.setText(currentTier.getName());
		
			currentGroupLbl.setText((currentGroupIndex()+1) + " ");
			
			currentCharLbl.setText(currentCharIndex() + " ");
		}
	}
	
	private void addSelectionHighlights(JTextComponent textComp, int recordIndex, String tierName, int groupIndex) {
		final List<SessionEditorSelection> selections = 
				getEditor().getSelectionModel().getSelectionsForGroup(recordIndex,
						tierName, groupIndex);
		final Highlighter hl = new GroupFieldHighlighter();
		textComp.setHighlighter(hl);
		for(SessionEditorSelection selection:selections) {
			final Range r = selection.getGroupRange();
			try {
				hl.addHighlight(r.getFirst(), r.getLast(), 
						new DefaultHighlighter.DefaultHighlightPainter(PhonGuiConstants.PHON_SELECTED));
			} catch (BadLocationException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
					"pref, fill:pref:grow(0.5), 5dlu, pref, fill:pref:grow, right:pref, 5dlu, right:pref",
					"pref");
			topPanel = new JPanel(layout);
			
			final DefaultComboBoxModel speakerBoxModel = new DefaultComboBoxModel();
			for(Participant participant:session.getParticipants()) {
				speakerBoxModel.addElement(participant);
			}
			speakerBox = new JComboBox(speakerBoxModel);
			speakerBox.setRenderer(speakerRenderer);
			speakerBox.addItemListener(speakerListener);
			
			final PhonUIAction excludeAct = new PhonUIAction(this, "onExclude");
			excludeAct.putValue(PhonUIAction.NAME, excludeFromSearchesText);
			excludeFromSearchesBox = new JCheckBox(excludeAct);
			
			final CellConstraints cc = new CellConstraints();
			topPanel.add(new JLabel("Speaker: "), cc.xy(1,1));
			topPanel.add(speakerBox, cc.xy(2,1));
			
			topPanel.add(excludeFromSearchesBox, cc.xy(4, 1));
			
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
			
			final DeleteGroupCommand delGroupAct = new DeleteGroupCommand(this);
			delGroupAct.putValue(DeleteGroupCommand.SHORT_DESCRIPTION, delGroupAct.getValue(DeleteGroupCommand.NAME));
			delGroupAct.putValue(DeleteGroupCommand.NAME, null);
			final JButton delGroupBtn = new JButton(delGroupAct);
			delGroupBtn.setFocusable(false);
			btnPanel.add(delGroupBtn);
			
			topPanel.add(btnPanel, cc.xy(6, 1));
			
			topPanel.add(layoutButtons, cc.xy(8, 1));
		}
		
		return topPanel;
	}
	
	private void updateRecordAlignment(int group) {
		final Record record = getEditor().currentRecord();
		final Tier<IPATranscript> ipaTarget = record.getIPATarget();
		final Tier<IPATranscript> ipaActual = record.getIPAActual();
		
		final IPATranscript targetGroup = (group < ipaTarget.numberOfGroups() ? ipaTarget.getGroup(group) : new IPATranscript());
		final IPATranscript actualGroup = (group < ipaActual.numberOfGroups() ? ipaActual.getGroup(group) : new IPATranscript());
		final PhoneAligner aligner = new PhoneAligner();
		final PhoneMap pm = aligner.calculatePhoneMap(targetGroup, actualGroup);
		
		final TierEdit<PhoneMap> pmEdit = new TierEdit<PhoneMap>(getEditor(), record.getPhoneAlignment(), group, pm);
		getEditor().getUndoSupport().postEdit(pmEdit);
		
		// we also need to send out a TIER_DATA_CHANGED event so the syllabification/alignment view updates
		final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, this, SystemTierType.SyllableAlignment.getName());
		getEditor().getEventManager().queueEvent(ee);
	}
	
	private final TierEditorListener tierEditorListener = new TierEditorListener
			() {
		
		@Override
		public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> tierEdit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			getEditor().getUndoSupport().postEdit(tierEdit);
		}

		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex,
				T newValue, T oldValue) {
			final EditorEvent ee = new EditorEvent(EditorEventType.TIER_CHANGED_EVT, RecordDataEditorView.this, tier.getName());
			getEditor().getEventManager().queueEvent(ee);
			
			// XXX
			// Special case for IPATranscript tiers
			// alignment should be updated here!
			if(SystemTierType.IPATarget.getName().equals(tier.getName()) ||
					SystemTierType.IPAActual.getName().equals(tier.getName())) {
				updateRecordAlignment(groupIndex);
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
				retVal.setText(participant.getName());
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
				if(selectedObj != null) {
					final Participant selectedSpeaker = Participant.class.cast(selectedObj);
					final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(editor, record, selectedSpeaker);
					editor.getUndoSupport().postEdit(edit);
				}
			}
		}
		
	};
	
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
	 * @param current tier or <code>null</code> if not
	 *  set
	 */
	public Tier<?> currentTier() {
		return (currentTierRef != null ? currentTierRef.get() : null);
	}
	
	/**
	 * Return the current group index.
	 * 
	 * @param the current group index, < 0 if not set
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
	
	private void fireSessionLocationChanged() {
		final EditorEvent ee = new EditorEvent(EditorEventType.SESSION_LOCATION_CHANGED_EVT, this,
				getSessionLocation());
		getEditor().getEventManager().queueEvent(ee);
	}
	
	/*
	 * Editor Actions
	 */
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
	public void onGroupsChange(EditorEvent event) {
		update();
		repaint();
	}
	
	@RunOnEDT
	public void onParticipantsChanged(EditorEvent event) {
		remove(topPanel);
		topPanel = null;
		topPanel = getTopPanel();
		add(topPanel, BorderLayout.NORTH);
		revalidate();
		repaint();
	}
	
	@RunOnEDT
	public void onSessionLocationChanged(EditorEvent event) {
		updateStatus();
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
			final JComponent comp = (JComponent)e.getSource();
			if(comp != null && comp instanceof JTextComponent) {
				final JTextComponent textComp = (JTextComponent)comp;
				if(textComp.getSelectedText() != null) {
					textComp.setSelectionStart(-1);
					textComp.setSelectionEnd(-1);
				}
			}
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

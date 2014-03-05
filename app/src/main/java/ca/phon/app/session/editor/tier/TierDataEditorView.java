package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.tier.layout.TierDataConstraint;
import ca.phon.app.session.editor.tier.layout.TierDataLayout;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutBgPainter;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutButtons;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutPanel;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.RecordExcludeEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Editor view for tier data.
 *
 */
public class TierDataEditorView extends EditorView {

	private static final long serialVersionUID = 2961561720211049250L;
	
	private final static String VIEW_NAME = "Record Data";
	
	/*
	 * Common data for all records
	 */
	private JPanel topPanel;

	/**
	 * speaker selection
	 */
	private JComboBox speakerBox;

	/**
	 * query exclusion
	 */
	private JCheckBox excludeFromSearchesBox;
	private final static String excludeFromSearchesText = "Exclude from searches";
	
	/** 
	 * content pane
	 */
	private TierDataLayoutPanel contentPane;
	
	public TierDataEditorView(SessionEditor editor) {
		super(editor);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		setBackground(Color.white);
		
//		final TierDataLayout layout = new TierDataLayout();
		contentPane = new TierDataLayoutPanel();
		contentPane.setBackground(Color.white);
		
		final JScrollPane scroller = new JScrollPane(contentPane);
		scroller.setBackground(Color.white);
		add(scroller, BorderLayout.CENTER);
		
		final JPanel panel = getTopPanel();
		add(panel, BorderLayout.NORTH);
		
		update();
		setupEditorActions();
	}
	
	private void setupEditorActions() {
		final EditorAction onTierViewChangeAct = 
				new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, onTierViewChangeAct);
	
		final EditorAction onRecordChangeAct =
				new DelegateEditorAction(this, "onRecordChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChangeAct);
	}
	
	/**
	 * Upadte the current tier view.
	 * 
	 */
	private void update() {
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
					final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tier, gIdx);
					tierEditor.addTierEditorListener(tierEditorListener);
					final Component tierComp = tierEditor.getEditorComponent();
					tierComp.setFont(tierFont);
					contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIdx, row));
				}
			} else {
				final TierEditor tierEditor = tierEditorFactory.createTierEditor(getEditor(), tier, 0);
				
				tierEditor.addTierEditorListener(tierEditorListener);
				final Component tierComp = tierEditor.getEditorComponent();
				tierComp.setFont(tierFont);
				contentPane.add(tierComp, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row));
			}
			row++;
		}
		revalidate();
	}
	
	private JPanel getTopPanel() {
		if(topPanel == null) {
			final TierDataLayout tdl = (TierDataLayout)contentPane.getLayout();
			final TierDataLayoutButtons layoutButtons = new TierDataLayoutButtons(contentPane, tdl);
			
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			
			final FormLayout layout = new FormLayout(
					"pref, fill:pref:grow(0.5), 5dlu, pref, fill:pref:grow, right:pref, right:pref",
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
			topPanel.add(new JLabel("Speaker"), cc.xy(1,1));
			topPanel.add(speakerBox, cc.xy(2,1));
			
			topPanel.add(excludeFromSearchesBox, cc.xy(4, 1));
			
			topPanel.add(layoutButtons, cc.xy(7, 1));
		}
		
		return topPanel;
	}
	
	private final TierEditorListener tierEditorListener = new TierEditorListener() {
		
		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			final TierEdit<T> tierEdit = new TierEdit<T>(getEditor(), tier, groupIndex, newValue);
			getEditor().getUndoSupport().postEdit(tierEdit);
		}
		
	};
	
	private final DefaultListCellRenderer speakerRenderer = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			final Participant participant = (Participant)value;
			retVal.setText(participant.getName());
			return retVal;
		}
		
	};
	
	private final ItemListener speakerListener = new ItemListener() {

		@Override
		public void itemStateChanged(ItemEvent e) {
			final Object selectedObj = speakerBox.getSelectedItem();
			final SessionEditor editor = getEditor();
			final Record record = editor.currentRecord();
			if(selectedObj != null) {
				final Participant selectedSpeaker = Participant.class.cast(selectedObj);
				final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(editor, record, selectedSpeaker);
				editor.getUndoSupport().postEdit(edit);
			}
		}
		
	};
	
	public void onExclude() {
		final boolean exclude = excludeFromSearchesBox.isSelected();
		final RecordExcludeEdit edit = new RecordExcludeEdit(getEditor(), getEditor().currentRecord(), exclude);
		getEditor().getUndoSupport().postEdit(edit);
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
		repaint();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/record", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}

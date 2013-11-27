package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.Group;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;

/**
 * Editor view for tier data.
 *
 */
public class TierDataPanel extends EditorView {

	private static final long serialVersionUID = 2961561720211049250L;
	
	private final static String VIEW_NAME = "Record Data";
	
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
	 * speaker tier
	 */
	private JPanel speakerTier;
	
	/** 
	 * content pane
	 */
	private JPanel contentPane;
	
	public TierDataPanel(SessionEditor editor) {
		super(editor);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		setBackground(Color.white);
		
		final TierDataLayout layout = new TierDataLayout();
		contentPane = new JPanel(layout);
		contentPane.setBackground(Color.white);
		
		final JScrollPane scroller = new JScrollPane(contentPane);
		scroller.setBackground(Color.white);
		add(scroller, BorderLayout.CENTER);
		
		update();
		setupEditorActions();
	}
	
	private void setupEditorActions() {
		final EditorAction onTierViewChangeAct = 
				new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, onTierViewChangeAct);
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
		
		final JLabel speakerLbl = new JLabel("Speaker");
		speakerLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
		speakerLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		contentPane.add(speakerLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0));
		
		final JPanel speakerTier = getSpeakerTier(record);
		contentPane.add(speakerTier, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, 0));
		
		final List<TierViewItem> tierView = session.getTierView();
		int row = 1;
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
			
			if(isGrouped) {
				final Tier<?> tier = record.getTier(tierName);
				for(int gIdx = 0; gIdx < record.numberOfGroups(); gIdx++) {
					
					final GroupField<?> textField = new GroupField<>(getEditor(), tier, gIdx);
					textField.getDocument().addDocumentListener(new DocumentListener() {
						
						@Override
						public void removeUpdate(DocumentEvent arg0) {
//							revalidate();
//							contentPane.invalidate();
							contentPane.getLayout().layoutContainer(contentPane);
						}
						
						@Override
						public void insertUpdate(DocumentEvent arg0) {
//							revalidate();
//							contentPane.invalidate();
							contentPane.getLayout().layoutContainer(contentPane);
						}
						
						@Override
						public void changedUpdate(DocumentEvent arg0) {
							// TODO Auto-generated method stub
							
						}
					});
					textField.setBorder(new GroupFieldBorder());
					contentPane.add(textField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN + gIdx, row));
				}
			} else {
				String tv = "";
				if(systemTier != null) {
					switch(systemTier) {
					case Notes:
						tv = record.getNotes().toString();
						break;
						
					case Segment:
						tv = record.getSegment().toString();
						break;
						
					default:
						break;
					}
				} else {
					tv = record.getTier(tierName, String.class).toString();
				}
				final JTextArea textArea = new JTextArea(tv);
				textArea.setWrapStyleWord(true);
				textArea.setLineWrap(true);
//				textArea.setRows(3);
				contentPane.add(textArea, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, row));
			}
			row++;
		}
		revalidate();
	}
	
	private JPanel getSpeakerTier(Record record) {
		if(speakerTier == null) {
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			
			final FormLayout layout = new FormLayout("fill:pref:grow, pref", "pref");
			speakerTier = new JPanel(layout);
			speakerTier.setBackground(Color.white);
			
			final DefaultComboBoxModel speakerBoxModel = new DefaultComboBoxModel();
			for(Participant participant:session.getParticipants()) {
				speakerBoxModel.addElement(participant);
			}
			speakerBox = new JComboBox(speakerBoxModel);
			speakerBox.setRenderer(speakerRenderer);
			
			excludeFromSearchesBox = new JCheckBox(excludeFromSearchesText);
			
			final CellConstraints cc = new CellConstraints();
			speakerTier.add(speakerBox, cc.xy(1,1));
			speakerTier.add(excludeFromSearchesBox, cc.xy(2,1));
		}
		
		if(record.getSpeaker() != null) {
			speakerBox.setSelectedItem(record.getSpeaker());
			excludeFromSearchesBox.setSelected(record.isExcludeFromSearches());
		}
		
		return speakerTier;
	}
	
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
	
	/*
	 * Editor Actions
	 */
	@RunOnEDT
	public void onTierViewChange(EditorEvent event) {
		update();
		repaint();
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

}

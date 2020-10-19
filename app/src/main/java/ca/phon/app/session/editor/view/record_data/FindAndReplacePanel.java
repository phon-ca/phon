package ca.phon.app.session.editor.view.record_data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorSelection;
import ca.phon.app.session.editor.search.FindExpr;
import ca.phon.app.session.editor.search.FindManager;
import ca.phon.app.session.editor.search.SearchType;
import ca.phon.app.session.editor.search.FindManager.FindDirection;
import ca.phon.app.session.editor.search.FindManager.FindStatus;
import ca.phon.app.session.editor.search.actions.FindNextAction;
import ca.phon.app.session.editor.search.actions.FindPrevAction;
import ca.phon.app.session.editor.search.actions.ReplaceAction;
import ca.phon.app.session.editor.search.actions.ReplaceAllAction;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.common.TierEditorListener;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierString;
import ca.phon.session.TierViewItem;
import ca.phon.session.position.GroupLocation;
import ca.phon.session.position.RecordLocation;
import ca.phon.session.position.SessionLocation;
import ca.phon.session.position.SessionRange;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.Language;

public class FindAndReplacePanel extends JPanel {

	private final static String ANY_TIER_NAME = "__any_tier__";
	
	private final static String FIND_TIER_NAME = "__find__";
	
	private final static String REPLACE_TIER_NAME = "__replace_tier__";
	
	// center panel
	private TierDataLayoutPanel tierView;
	private JComboBox<String> tierNameBox;
	private GroupField<String> findField;
	private Tier<String> findTier;
	private FindOptionsPanel findOptsPanel;
	private GroupField<String> replaceField;
	private Tier<String> replaceTier;
	
	// rigt-hand side
	private JPanel findActionsPanel;
	private JCheckBox wrapBox;
	private JButton nextBtn;
	private JButton prevBtn;
	private JButton replaceBtn;
	private JButton replaceFindBtn;
	private JButton replaceAllBtn;
	
	private SessionEditor editor;
	
	// find manager
	private FindManager findManager;
	
	public FindAndReplacePanel(SessionEditor sessionEditor) {
		super();
		
		this.editor = sessionEditor;
		findManager = new FindManager(sessionEditor.getSession());
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final SessionFactory factory = SessionFactory.newFactory();
		findTier = factory.createTier(FIND_TIER_NAME, String.class, false);
		findField = new GroupField<String>(findTier, 0);
		findField.addTierEditorListener(tierEditorListener);
		
		replaceTier = factory.createTier(REPLACE_TIER_NAME, String.class, false);
		replaceField = new GroupField<String>(replaceTier, 0);
		replaceField.addTierEditorListener(tierEditorListener);
		
		tierNameBox = new JComboBox<>();
		tierNameBox.setRenderer(new TierNameCellRenderer());
		updateTierNameBox();
		
		findOptsPanel = new FindOptionsPanel();
		
		tierView = new TierDataLayoutPanel();
		updateTierView();
		
		setupSidePanel();
		
		add(tierView, BorderLayout.CENTER);
		add(findActionsPanel, BorderLayout.EAST);
	}
	
	public FindManager getFindManager() {
		return this.findManager;
	}
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
	private void setupSidePanel() {
		findActionsPanel = new JPanel(new GridLayout(3, 2));
		
		nextBtn = new JButton(new FindNextAction(getEditor(), this));
		prevBtn = new JButton(new FindPrevAction(getEditor(), this));
		replaceBtn = new JButton(new ReplaceAction(getEditor(), this, false));
		replaceFindBtn = new JButton(new ReplaceAction(getEditor(), this, true));
		replaceAllBtn = new JButton(new ReplaceAllAction(getEditor(), this));
		
		wrapBox = new JCheckBox("Wrap search");
		wrapBox.setToolTipText("Wrap search when beginning/end of session is reached");
		wrapBox.setSelected(false);
		
		findActionsPanel.add(nextBtn);
		findActionsPanel.add(prevBtn);
		findActionsPanel.add(replaceBtn);
		findActionsPanel.add(replaceFindBtn);
		findActionsPanel.add(replaceAllBtn);
		findActionsPanel.add(wrapBox);
	}
	
	private void updateTierNameBox() {
		// update tier name combo box
		int selectedIdx = tierNameBox.getSelectedIndex();
		String selected = (selectedIdx >= 0 ? tierNameBox.getItemAt(selectedIdx).toString() : null);
		List<String> tierNames = new ArrayList<>();
		tierNames.add("Any tier");
		getEditor().getSession().getTierView()
			.stream()
			.filter( TierViewItem::isVisible )
			.filter( (tvi) -> !SystemTierType.Segment.getName().equals(tvi.getTierName()) )
			.map( TierViewItem::getTierName )
			.forEach( tierNames::add );
		ComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(tierNames.toArray(new String[0]));
		tierNameBox.setModel(comboBoxModel);
		if(selected != null && tierNames.contains(selected)) {
			tierNameBox.setSelectedItem(selected);
		} else {
			tierNameBox.setSelectedIndex(0);
		}
	}
	
	private void updateTierView() {
		tierView.removeAll();
		
		tierView.add(tierNameBox, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 0));
		tierView.add(findField, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, 0));
		tierView.add(findOptsPanel, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, 1));

		JLabel replaceLbl = new JLabel("Replace");
		replaceLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		tierView.add(replaceLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, 2));
		tierView.add(replaceField, new TierDataConstraint(TierDataConstraint.FLAT_TIER_COLUMN, 2));
	}
	
	/* Find and replace methods */
	private FindExpr getFindExpr() {
		final Tier<String> tier = findTier;
		if(tier == null || tier.getGroup(0).length() == 0) return null;
				
		final FindExpr retVal = new FindExpr();
		retVal.setExpr(tier.getGroup(0));
		retVal.setCaseSensitive(findOptsPanel.caseSensitiveBox.isSelected());
		retVal.setType((SearchType)findOptsPanel.typeBox.getSelectedItem());
		
		return retVal;
	}
	
	private List<String> getSearchTiers() {
		if(tierNameBox.getSelectedIndex() == 0) {
			List<String> tierNames = new ArrayList<>();
			for(int i = 1; i < tierNameBox.getModel().getSize(); i++) {
				tierNames.add(tierNameBox.getModel().getElementAt(i));
			}
			return tierNames;
		} else {
			return List.of(tierNameBox.getSelectedItem().toString());
		}
	}
		
	private void setupFindManager(FindManager findManager) {
		String queryText = findTier.getGroup(0);
		
		if(tierNameBox.getSelectedIndex() == 0) {
			findManager.setAnyExpr(getFindExpr());
		} else {
			findManager.setAnyExpr(null);
			findManager.setExprForTier(tierNameBox.getSelectedItem().toString(), getFindExpr());
		}
		
		findManager.setSearchTier(getSearchTiers());
	}
	
	/*
	 * Return the 'start' location of the session.
	 * 
	 * @return startLocation
	 */
	private SessionLocation startLocation() {
		final FindManager findManager = getFindManager();
		
		final String tier = 
				(findManager.getSearchTiers().length > 0 ? findManager.getSearchTiers()[0] :
					SystemTierType.Orthography.getName());
		
		return new SessionLocation(0, 
				new RecordLocation(tier, new GroupLocation(0, 0)));
	}

	/*
	 * Return the end location when searching this session.
	 *  
	 * @return endLocation
	 */
	private SessionLocation endLocation() {
		final Session session = getEditor().getSession();
		final FindManager findManager = getFindManager();
		
		final String tierName = 
				(findManager.getSearchTiers().length > 0 ? findManager.getSearchTiers()[findManager.getSearchTiers().length-1] :
					SystemTierType.Notes.getName());
		final Record r = session.getRecord(session.getRecordCount()-1);
		final Tier<String> tier = r.getTier(tierName, String.class);
		final String grp = tier.getGroup(tier.numberOfGroups()-1);
		
		return new SessionLocation(session.getRecordCount()-1, new RecordLocation(tierName, 
				new GroupLocation(tier.numberOfGroups()-1, grp.length())));
	}
	
	private void setupSessionSelection(SessionRange sessionRange) {
		final SessionEditorSelection selection = 
				new SessionEditorSelection(sessionRange.getRecordIndex(), sessionRange.getRecordRange().getTier(),
						sessionRange.getRecordRange().getGroupRange().getGroupIndex(),
						sessionRange.getRecordRange().getGroupRange().getRange());
		getEditor().getSelectionModel().setSelection(selection);
		getEditor().setCurrentRecordIndex(sessionRange.getRecordIndex());
	}
	
	public void findNext() {
		final FindManager findManager = getFindManager();
		setupFindManager(findManager);
		
		if(findManager.getCurrentLocation() == null) {
			findManager.setCurrentLocation(
					findManager.getDirection() == FindDirection.FORWARDS ? startLocation() : endLocation());
		}
		
		SessionRange nextInstance = findManager.findNext();
		if(nextInstance != null) {
			setupSessionSelection(nextInstance);
		} else if(findManager.getDirection() == FindDirection.FORWARDS &&
				findManager.getStatus() == FindStatus.HIT_END) {
			if(wrapBox.isSelected()) {
				findManager.setCurrentLocation(startLocation());
				nextInstance = findManager.findNext();
				if(nextInstance == null) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					setupSessionSelection(nextInstance);
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		} else if(findManager.getDirection() == FindDirection.BACKWARDS &&
				findManager.getStatus() == FindStatus.HIT_BEGINNING) {
			if(wrapBox.isSelected()) {
				findManager.setCurrentLocation(endLocation());
				nextInstance = findManager.findNext();
				if(nextInstance == null) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					setupSessionSelection(nextInstance);
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
	
	public void findPrev() {
		final FindManager findManager = getFindManager();
		setupFindManager(findManager);
		
		if(findManager.getCurrentLocation() == null) {
			findManager.setCurrentLocation(
					findManager.getDirection() == FindDirection.FORWARDS ? endLocation() : startLocation());
		}
		
		
		SessionRange nextInstance = findManager.findPrev();
		if(nextInstance != null) {
			setupSessionSelection(nextInstance);
		} else if(findManager.getDirection() == FindDirection.FORWARDS &&
				findManager.getStatus() == FindStatus.HIT_BEGINNING) {
			if(wrapBox.isSelected()) {
				findManager.setCurrentLocation(endLocation());
				nextInstance = findManager.findPrev();
				if(nextInstance == null) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					setupSessionSelection(nextInstance);
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		} else if(findManager.getDirection() == FindDirection.BACKWARDS &&
				findManager.getStatus() == FindStatus.HIT_END) {
			if(wrapBox.isSelected()) {
				findManager.setCurrentLocation(startLocation());
				nextInstance = findManager.findPrev();
				if(nextInstance == null) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					setupSessionSelection(nextInstance);
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
	
	public void replace() {
		final FindManager findManager = getFindManager();
		setupFindManager(findManager);
		
		if(findManager.getMatchedExpr() != null && findManager.getMatchedRange() != null) {
			if(replaceTier != null && replaceTier.numberOfGroups() > 0) {
				final String replaceExpr = replaceTier.getGroup(0);
				final Object newVal = findManager.getMatchedExpr().replace(replaceExpr);
				
				// re-syllabify if an IPA tier
				final SessionRange sr = findManager.getMatchedRange();
				
				final Record record = getEditor().getSession().getRecord(sr.getRecordIndex());
				
				@SuppressWarnings({ "unchecked", "rawtypes" })
				final TierEdit<?> tierEdit = new TierEdit(getEditor(), 
						getEditor().currentRecord().getTier(sr.getRecordRange().getTier()),
						sr.getRecordRange().getGroupRange().getGroupIndex(), newVal);
				
				if(newVal instanceof IPATranscript) {
					final IPATranscript ipa = (IPATranscript)newVal;
					final Syllabifier syllabifier = getSyllabifier(sr.getRecordRange().getTier());
					if(syllabifier != null) {
						syllabifier.syllabify(ipa.toList());
					}
					
					// update alignment
					
					final CompoundEdit edit = new CompoundEdit();
					final PhoneMap pm = (new PhoneAligner()).calculatePhoneMap(
							record.getIPATarget().getGroup(sr.getRecordRange().getGroupRange().getGroupIndex()),
							record.getIPAActual().getGroup(sr.getRecordRange().getGroupRange().getGroupIndex()));
					final TierEdit<PhoneMap> alignmentEdit = 
							new TierEdit<PhoneMap>(getEditor(), record.getPhoneAlignment(), 
									sr.getRecordRange().getGroupRange().getGroupIndex(), pm);
					tierEdit.doIt();
					edit.addEdit(tierEdit);
					alignmentEdit.doIt();
					edit.addEdit(alignmentEdit);
					edit.end();
					getEditor().getUndoSupport().postEdit(edit);
				} else {
					getEditor().getUndoSupport().postEdit(tierEdit);
				}
				
				getEditor().getSelectionModel().clear();
				
				
			}
		}
	}
	
	private Syllabifier getSyllabifier(String tier) {
		Syllabifier retVal = SyllabifierLibrary.getInstance().defaultSyllabifier();
		final Session session = getEditor().getSession();
		final SyllabifierInfo info = session.getExtension(SyllabifierInfo.class);
		if(info != null) {
			final Language lang = info.getSyllabifierLanguageForTier(tier);
			if(lang != null && SyllabifierLibrary.getInstance().availableSyllabifierLanguages().contains(lang)) {
				retVal = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(lang);
			}
		}
		return retVal;
	}
	
	public void replaceAll() {
		final String replaceExpr = replaceTier.getGroup(0);

		// create a new find manager
		final Session session = getEditor().getSession();
		final FindManager findManager = new FindManager(session);
		setupFindManager(findManager);
		final SessionLocation startLoc = 
				new SessionLocation(0, new RecordLocation(findManager.getSearchTiers()[0],
						new GroupLocation(0, 0)));
		findManager.setCurrentLocation(startLoc);
		
		
		final CompoundEdit edit = new CompoundEdit();
		int occurrences = 0;
		
		SessionRange currentRange = null;
		while((currentRange = findManager.findNext()) != null) {
			++occurrences;
			
			final Record r = session.getRecord(currentRange.getRecordIndex());
			final Tier<?> tier = r.getTier(currentRange.getRecordRange().getTier());
			
			final Object newVal = findManager.getMatchedExpr().replace(replaceExpr);
			
			// re-syllabify if an IPA tier
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final TierEdit<?> tierEdit = new TierEdit(getEditor(), tier,
					currentRange.getRecordRange().getGroupRange().getGroupIndex(), newVal);
			tierEdit.doIt();
			edit.addEdit(tierEdit);
			
			if(newVal instanceof IPATranscript) {
				final IPATranscript ipa = (IPATranscript)newVal;
				final Syllabifier syllabifier = getSyllabifier(currentRange.getRecordRange().getTier());
				if(syllabifier != null) {
					syllabifier.syllabify(ipa.toList());
				}
				
				// update alignment
				
				final PhoneMap pm = (new PhoneAligner()).calculatePhoneMap(
						r.getIPATarget().getGroup(currentRange.getRecordRange().getGroupRange().getGroupIndex()),
						r.getIPAActual().getGroup(currentRange.getRecordRange().getGroupRange().getGroupIndex()));
				final TierEdit<PhoneMap> alignmentEdit = 
						new TierEdit<PhoneMap>(getEditor(), r.getPhoneAlignment(), 
								currentRange.getRecordRange().getGroupRange().getGroupIndex(), pm);
				alignmentEdit.doIt();

				edit.addEdit(alignmentEdit);
				edit.end();
			}
		}
		edit.end();
		
		if(occurrences > 0) {
			final EditorEvent ee = new EditorEvent(EditorEventType.MODIFICATION_EVENT, this);
			getEditor().getEventManager().queueEvent(ee);
			final EditorEvent refresh = new EditorEvent(EditorEventType.RECORD_REFRESH_EVT, this);
			getEditor().getEventManager().queueEvent(refresh);
			
			getEditor().getUndoSupport().postEdit(edit);
		}
		
		final String message = 
				"Replaced " + occurrences + " occurrences with " + replaceExpr;
		final Toast toast = ToastFactory.makeToast(message);
		toast.start(replaceAllBtn);
	}
	
	private class TierNameCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			retVal.setHorizontalAlignment(SwingConstants.RIGHT);
			return retVal;
		}
		
	}
	
	private class FindOptionsPanel extends JPanel {
		
		JCheckBox caseSensitiveBox;
		
		JComboBox<SearchType> typeBox;
		
		public FindOptionsPanel() {
			super();
			init();
			
			setOpaque(false);
			caseSensitiveBox.setOpaque(false);
		}
		
		private void init() {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			
			caseSensitiveBox = new JCheckBox("Case sensitive");
			caseSensitiveBox.setOpaque(false);
			add(caseSensitiveBox);
			
			typeBox = new JComboBox<>(SearchType.values());
			typeBox.setSelectedItem(SearchType.PLAIN);
			add(typeBox);
		}
		
	}
	
	private final TierEditorListener tierEditorListener = new TierEditorListener() {
		
		@Override
		public <T> void tierValueChanged(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			
		}
		
		@Override
		public <T> void tierValueChange(Tier<T> tier, int groupIndex, T newValue,
				T oldValue) {
			tier.setGroup(groupIndex, newValue);
//			if(findManager != null) {
//				if(tier.getName().equals(ANY_TIER_NAME)) {
//					findManager.setAnyExpr(getAnyTierExpr());
//				} else {
//					findManager.setExprForTier(tier.getName(), exprForTier(tier.getName()));
//				}
//				findManager.setSearchTier(getSearchTiers());
//			}
		}
		
	};
}

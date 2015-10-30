/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor.view.find_and_replace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.undo.CompoundEdit;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorSelection;
import ca.phon.app.session.editor.search.SearchType;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.common.TierEditorListener;
import ca.phon.app.session.editor.view.find_and_replace.FindManager.FindDirection;
import ca.phon.app.session.editor.view.find_and_replace.FindManager.FindStatus;
import ca.phon.app.session.editor.view.find_and_replace.actions.FindNextAction;
import ca.phon.app.session.editor.view.find_and_replace.actions.FindPrevAction;
import ca.phon.app.session.editor.view.find_and_replace.actions.ReplaceAction;
import ca.phon.app.session.editor.view.find_and_replace.actions.ReplaceAllAction;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.session.GroupLocation;
import ca.phon.session.Record;
import ca.phon.session.RecordLocation;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SessionLocation;
import ca.phon.session.SessionRange;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.Language;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * An {@link SessionEditor} {@link EditorView} implementing Find & Replace
 * for a {@link Session}.
 *
 */
public class FindAndReplaceEditorView extends EditorView {
	
	private static final long serialVersionUID = 3981954934024480576L;

	public final static String VIEW_NAME = "Find & Replace";
	
	public final static String VIEW_ICON = "actions/edit-find-replace";
	
	private final static String ANY_TIER_NAME = "__any_tier__";
	
	private final static String REPLACE_TIER_NAME = "__replace_tier__";
	
	private final Map<String, Tier<String>> searchTiers = new LinkedHashMap<String, Tier<String>>();
	
	private final Map<String, FindOptionsPanel> searchOptions = new LinkedHashMap<String, FindOptionsPanel>();
	
	private final FindManager findManager;

	/* UI */
	private JPanel sidePanel;
	
	private TierDataLayoutPanel tierPanel;
	
	private JCheckBox wrapBox;
	
	private JButton nextBtn;
	
	private JButton prevBtn;
	
	private JButton replaceBtn;
	
	private JButton replaceFindBtn;
	
	private JButton replaceAllBtn;
	
	public FindAndReplaceEditorView(SessionEditor editor) {
		super(editor);
		
		init();
		updateTierView();
		setupEditorActions();
		
		findManager = new FindManager(editor.getSession());
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setBackground(Color.white);
		
		sidePanel = new JPanel();
		sidePanel.setBackground(PhonGuiConstants.PHON_SHADED);
		sidePanel.setOpaque(true);
		sidePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		setupSidePanel();
		add(sidePanel, BorderLayout.EAST);

		tierPanel = new TierDataLayoutPanel();
		
		final JScrollPane scroller = new JScrollPane(tierPanel);
		add(scroller, BorderLayout.CENTER);
	}
	
	private void setupSidePanel() {
		final VerticalLayout layout = new VerticalLayout(5);
		sidePanel.setLayout(layout);
		
		nextBtn = new JButton(new FindNextAction(getEditor(), this));
		prevBtn = new JButton(new FindPrevAction(getEditor(), this));
		replaceBtn = new JButton(new ReplaceAction(getEditor(), this, false));
		replaceFindBtn = new JButton(new ReplaceAction(getEditor(), this, true));
		replaceAllBtn = new JButton(new ReplaceAllAction(getEditor(), this));
		
		wrapBox = new JCheckBox("Wrap");
		wrapBox.setToolTipText("Wrap search");
		wrapBox.setSelected(false);
		
		sidePanel.add(nextBtn);
		sidePanel.add(prevBtn);
		sidePanel.add(replaceBtn);
		sidePanel.add(replaceFindBtn);
		sidePanel.add(replaceAllBtn);
		sidePanel.add(wrapBox);
		
		final ButtonGroup directionGrp = new ButtonGroup();
		
		final PhonUIAction setDirectionForwardsAct = new PhonUIAction(this, "setDirection", FindManager.FindDirection.FORWARDS);
		setDirectionForwardsAct.putValue(PhonUIAction.NAME, "Search Forwards");
		setDirectionForwardsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Search forwards in session");
		setDirectionForwardsAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.TRUE);
		final JRadioButton forwardsBtn = new JRadioButton(setDirectionForwardsAct);
		directionGrp.add(forwardsBtn);
		
		final PhonUIAction setDirectionBackwardAct = new PhonUIAction(this, "setDirection", FindManager.FindDirection.BACKWARDS);
		setDirectionBackwardAct.putValue(PhonUIAction.NAME, "Search Backwards");
		setDirectionBackwardAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Search backwards in session");
		setDirectionBackwardAct.putValue(PhonUIAction.SELECTED_KEY, Boolean.FALSE);
		final JRadioButton backwardsBtn = new JRadioButton(setDirectionBackwardAct);
		directionGrp.add(backwardsBtn);
		
		sidePanel.add(forwardsBtn);
		sidePanel.add(backwardsBtn);
	}
	
	private void setupEditorActions() {
		final EditorAction viewChangedAct = 
				new DelegateEditorAction(this, "onTierViewChanged");
		getEditor().getEventManager().registerActionForEvent(
				EditorEventType.TIER_VIEW_CHANGED_EVT, viewChangedAct);
		
		final EditorAction sessionLocationChangedAct = 
				new DelegateEditorAction(this, "onSessionLocationChanged");
		getEditor().getEventManager().registerActionForEvent(
				EditorEventType.SESSION_LOCATION_CHANGED_EVT, sessionLocationChangedAct);	
	}

	private void updateTierView() {
		tierPanel.removeAll();
		int rowIdx = 0;
		
		final SessionFactory factory = SessionFactory.newFactory();
		Tier<String> anyTier = searchTiers.get(ANY_TIER_NAME);
		if(anyTier == null) {
			anyTier = factory.createTier(ANY_TIER_NAME, String.class, true);
			anyTier.addGroup();
			searchTiers.put(ANY_TIER_NAME, anyTier);
		}
		FindOptionsPanel anyOptsPanel = searchOptions.get(ANY_TIER_NAME);
		if(anyOptsPanel == null) {
			anyOptsPanel = new FindOptionsPanel();
			final FindOptionsListener anyOptsListener = new FindOptionsListener(ANY_TIER_NAME);
			anyOptsListener.install(anyOptsPanel);
			searchOptions.put(ANY_TIER_NAME, anyOptsPanel);
		}
		final GroupField<String> anyTierField = new GroupField<String>(anyTier, 0);
		anyTierField.setFont(FontPreferences.getTierFont());
		anyTierField.addTierEditorListener(tierEditorListener);
		final JLabel anyTierLbl = new JLabel("Any tier");
		anyTierLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		
		tierPanel.add(anyTierLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		tierPanel.add(anyTierField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN, rowIdx));
		tierPanel.add(anyOptsPanel, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+1, rowIdx++));
		
		// add tier specific search fields
		final Session session = getEditor().getSession();
		final List<TierViewItem> tierView = session.getTierView();
		for(TierViewItem tvi:tierView) {
			final SystemTierType systemTier = SystemTierType.tierFromString(tvi.getTierName());
			if(systemTier != null && systemTier == SystemTierType.Segment) {
				continue;
			}
			if(!tvi.isVisible()) continue;
			
			Tier<String> tier = searchTiers.get(tvi.getTierName());
			if(tier == null) {
				tier = factory.createTier(tvi.getTierName(), String.class, true);
				tier.addGroup();
				searchTiers.put(tvi.getTierName(), tier);
			}
			FindOptionsPanel optsPanel = searchOptions.get(tvi.getTierName());
			if(optsPanel == null) {
				optsPanel = new FindOptionsPanel();
				final Class<?> tierType = (systemTier != null ? systemTier.getDeclaredType() : String.class);
				if(tierType != IPATranscript.class) {
					optsPanel.typeBox.removeItem(SearchType.PHONEX);
				} else {
					optsPanel.typeBox.setSelectedItem(SearchType.PHONEX);
					optsPanel.caseSensitiveBox.setEnabled(false);
				}
				final FindOptionsListener optsListener = new FindOptionsListener(tier.getName());
				optsListener.install(optsPanel);
				
				searchOptions.put(tvi.getTierName(), optsPanel);
			}
			final GroupField<String> tierField = new GroupField<String>(tier, 0);
			
			final String fontString = tvi.getTierFont();
			Font tierFont = FontPreferences.getTierFont();
			if(fontString != null && !fontString.equalsIgnoreCase("default")) {
				tierFont = Font.decode(fontString);
			}
			tierField.setFont(tierFont);
			
			tierField.addTierEditorListener(tierEditorListener);
			final JLabel tierLbl = new JLabel(tvi.getTierName());
			tierLbl.setHorizontalAlignment(SwingConstants.RIGHT);
			
			tierPanel.add(tierLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
			tierPanel.add(tierField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN, rowIdx));
			tierPanel.add(optsPanel, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+1, rowIdx++));
		}
		
		// add separator
		final JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		tierPanel.add(sep, new TierDataConstraint(TierDataConstraint.FULL_TIER_COLUMN, rowIdx++));
		
		Tier<String> replaceTier = searchTiers.get(REPLACE_TIER_NAME);
		if(replaceTier == null) {
			replaceTier = factory.createTier(REPLACE_TIER_NAME, String.class, true);
			replaceTier.addGroup();
			searchTiers.put(REPLACE_TIER_NAME, replaceTier);
		}
		final GroupField<String> replaceTierField = new GroupField<String>(replaceTier, 0);
		replaceTierField.setFont(FontPreferences.getTierFont());
		replaceTierField.addTierEditorListener(tierEditorListener);
		final JLabel replaceLbl = new JLabel("Replace");
		replaceLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		
		tierPanel.add(replaceLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		tierPanel.add(replaceTierField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN, rowIdx++));
	}
	
	/* Editor Actions */
	@RunOnEDT
	public void onTierViewChanged(EditorEvent ee) {
		updateTierView();
		getFindManager().setSearchTier(getSearchTiers());
	}
	
	@RunOnEDT
	public void onSessionLocationChanged(EditorEvent ee) {
		final SessionLocation location = 
				(SessionLocation)ee.getEventData();
		if(location != null) {
			findManager.setCurrentLocation(location);
		}
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
		final JMenu retVal = new JMenu();
		
		retVal.add(new FindNextAction(getEditor(), this));
		retVal.add(new FindPrevAction(getEditor(), this));
		retVal.add(new ReplaceAction(getEditor(), this, false));
		retVal.add(new ReplaceAction(getEditor(), this, true));
		retVal.add(new ReplaceAllAction(getEditor(), this));
		
		return retVal;
	}
	
	public FindExpr getAnyTierExpr() {
		return exprForTier(ANY_TIER_NAME);
	}
	
	public FindExpr exprForTier(String tierName) {
		final Tier<String> tier = searchTiers.get(tierName);
		if(tier == null || tier.getGroup(0).length() == 0) return null;
		
		final FindOptionsPanel optsPanel = searchOptions.get(tierName);
		if(optsPanel == null) return null;
		
		final FindExpr retVal = new FindExpr();
		retVal.setExpr(tier.getGroup(0));
		retVal.setCaseSensitive(optsPanel.caseSensitiveBox.isSelected());
		retVal.setType((SearchType)optsPanel.typeBox.getSelectedItem());
		
		return retVal;
	}
	
	public FindManager getFindManager() {
		return findManager;
	}
	
	private List<String> getSearchTiers() {
		final Session session = getEditor().getSession();
		final List<TierViewItem> tierView = session.getTierView();
		final List<String> findManagerTiers = new ArrayList<String>();
		final String anyExpr = searchTiers.get(ANY_TIER_NAME).getGroup(0);
		if(anyExpr.length() != 0) {
			// add all tiers to find manager
			for(TierViewItem tvi:tierView) {
				final SystemTierType systemTier = SystemTierType.tierFromString(tvi.getTierName());
				if(systemTier == SystemTierType.Segment) {
					continue;
				}
				if(!tvi.isVisible()) continue;
				findManagerTiers.add(tvi.getTierName());
			}
		} else {
			// only add tiers that have expressions
			for(TierViewItem tvi:tierView) {
				if(!tvi.isVisible()) continue;
				final FindExpr expr = exprForTier(tvi.getTierName());
				if(expr != null) {
					findManagerTiers.add(tvi.getTierName());
				}
			}
		}
		return findManagerTiers;
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
	
	/*
	 * Actions 
	 */
	public void findNext() {
		final FindManager findManager = getFindManager();
		
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
		
		if(findManager.getMatchedExpr() != null && findManager.getMatchedRange() != null) {
			final Tier<String> replaceTier = searchTiers.get(REPLACE_TIER_NAME);
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
		final Tier<String> replaceTier = searchTiers.get(REPLACE_TIER_NAME);
		final String replaceExpr = replaceTier.getGroup(0);

		// create a new find manager
		final Session session = getEditor().getSession();
		final FindManager findManager = new FindManager(session);
		findManager.setAnyExpr(getAnyTierExpr());
		findManager.setSearchTier(getSearchTiers());
		for(String searchTier:findManager.getSearchTiers()) {
			findManager.setExprForTier(searchTier, exprForTier(searchTier));
		}
		findManager.setDirection(FindDirection.FORWARDS);
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
	
	public void setDirection(FindManager.FindDirection direction) {
		getFindManager().setDirection(direction);
	}
	
	private void setupSessionSelection(SessionRange sessionRange) {
		final SessionEditorSelection selection = 
				new SessionEditorSelection(sessionRange.getRecordIndex(), sessionRange.getRecordRange().getTier(),
						sessionRange.getRecordRange().getGroupRange().getGroupIndex(),
						sessionRange.getRecordRange().getGroupRange().getRange());
		getEditor().getSelectionModel().setSelection(selection);
		getEditor().setCurrentRecordIndex(sessionRange.getRecordIndex());
	}
	
	private SessionLocation getSessionLocation() {
		SessionLocation retVal = null;
		
		if(getEditor().getViewModel().isShowing(RecordDataEditorView.VIEW_NAME)) {
			final RecordDataEditorView recordDataView = 
					(RecordDataEditorView)getEditor().getViewModel().getView(RecordDataEditorView.VIEW_NAME);
			if(recordDataView.currentTier() != null) {
				retVal = recordDataView.getSessionLocation();
			}
		}
		
		if(retVal == null) {
			final String[] searchTiers = findManager.getSearchTiers();
			
			final GroupLocation grpLocation = new GroupLocation(0, 0);
			grpLocation.setGroupIndex(0);
			grpLocation.setCharIndex(0);
			final RecordLocation recordLocation = new RecordLocation(
					(searchTiers.length > 0 ? searchTiers[0] : SystemTierType.Orthography.getName()), grpLocation);
			retVal = new SessionLocation(getEditor().getCurrentRecordIndex(), recordLocation);
		}
		
		return retVal;
	}

	private class FindOptionsPanel extends JPanel {
		
		JCheckBox caseSensitiveBox;
		
		JComboBox typeBox;
		
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
			
			typeBox = new JComboBox(SearchType.values());
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
			if(findManager != null) {
				if(tier.getName().equals(ANY_TIER_NAME)) {
					findManager.setAnyExpr(getAnyTierExpr());
				} else {
					findManager.setExprForTier(tier.getName(), exprForTier(tier.getName()));
				}
				findManager.setSearchTier(getSearchTiers());
			}
		}
		
	};
	
	private class FindOptionsListener implements ItemListener, ActionListener {
		
		private String tierName;
		
		private FindOptionsPanel panel;
		
		public FindOptionsListener(String tierName) {
			this.tierName = tierName;
		}
		
		private void install(FindOptionsPanel panel) {
			this.panel = panel;
			panel.typeBox.addItemListener(this);
			panel.caseSensitiveBox.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setupExpr();
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			setupExpr();
			panel.caseSensitiveBox.setEnabled(panel.typeBox.getSelectedItem() != SearchType.PHONEX);
		}
		
		private void setupExpr() {
			FindExpr expr = null;
			if(tierName.equals(ANY_TIER_NAME)) {
				expr = findManager.getAnyExpr();
			} else {
				expr = findManager.getExprForTier(tierName);
			}
			if(expr != null) {
				expr.setCaseSensitive(panel.caseSensitiveBox.isSelected());
				expr.setType((SearchType)panel.typeBox.getSelectedItem());
			}
		}
		
	}
	
}

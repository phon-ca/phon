package ca.phon.app.session.editor.view.find_and_replace;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.EditorViewCategory;
import ca.phon.app.session.editor.EditorViewInfo;
import ca.phon.app.session.editor.RecordEditorPerspective;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionEditorSelection;
import ca.phon.app.session.editor.view.common.GroupField;
import ca.phon.app.session.editor.view.common.TierDataConstraint;
import ca.phon.app.session.editor.view.common.TierDataLayoutPanel;
import ca.phon.app.session.editor.view.common.TierEditorListener;
import ca.phon.app.session.editor.view.find_and_replace.FindManager.FindDirection;
import ca.phon.app.session.editor.view.find_and_replace.FindManager.FindStatus;
import ca.phon.app.session.editor.view.find_and_replace.actions.FindNextAction;
import ca.phon.app.session.editor.view.find_and_replace.actions.FindPrevAction;
import ca.phon.app.session.editor.view.find_and_replace.actions.ReplaceAction;
import ca.phon.app.session.editor.view.record_data.RecordDataEditorView;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.TierViewItem;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class FindAndReplaceEditorView extends EditorView {
	
	private static final long serialVersionUID = 3981954934024480576L;

	public final static String VIEW_NAME = "Find & Replace";
	
	public final static String VIEW_ICON = "actions/edit-find-replace";
	
	private JToolBar toolBar;
	
	private TierDataLayoutPanel tierPanel;
	
	private final static String ANY_TIER_NAME = "__any_tier__";
	
	private final static String REPLACE_TIER_NAME = "__replace_tier__";
	
	private final Map<String, Tier<String>> searchTiers = new LinkedHashMap<String, Tier<String>>();
	
	private final Map<String, FindOptionsPanel> searchOptions = new LinkedHashMap<String, FindOptionsPanel>();
	
	public FindAndReplaceEditorView(SessionEditor editor) {
		super(editor);
		
		init();
		setupToolbar();
		updateTierView();
		setupEditorActions();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.NORTH);

		tierPanel = new TierDataLayoutPanel();
		add(tierPanel, BorderLayout.CENTER);
	}
	
	private void setupToolbar() {
		toolBar.add(new FindNextAction(getEditor(), this));
		toolBar.add(new FindPrevAction(getEditor(), this));
		toolBar.add(new ReplaceAction(getEditor(), this, false));
		toolBar.add(new ReplaceAction(getEditor(), this, true));
	}
	
	private void setupEditorActions() {
		final EditorAction viewChangedAct = new DelegateEditorAction(this, "onTierViewChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, viewChangedAct);
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
			searchOptions.put(ANY_TIER_NAME, anyOptsPanel);
		}
		final GroupField<String> anyTierField = new GroupField<String>(anyTier, 0);
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
			
			Tier<String> tier = searchTiers.get(tvi.getTierName());
			if(tier == null) {
				tier = factory.createTier(tvi.getTierName(), String.class, true);
				tier.addGroup();
				searchTiers.put(tvi.getTierName(), tier);
			}
			FindOptionsPanel optsPanel = searchOptions.get(tvi.getTierName());
			if(optsPanel == null) {
				optsPanel = new FindOptionsPanel();
				searchOptions.put(tvi.getTierName(), optsPanel);
			}
			final GroupField<String> tierField = new GroupField<String>(tier, 0);
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
		replaceTierField.addTierEditorListener(tierEditorListener);
		final JLabel replaceLbl = new JLabel("Replace");
		replaceLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		
		tierPanel.add(replaceLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, rowIdx));
		tierPanel.add(replaceTierField, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN, rowIdx++));
	}
	
	/* Editor Actions */
	public void onTierViewChanged(EditorEvent ee) {
		updateTierView();
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
		retVal.setType((FindExpr.SearchType)optsPanel.typeBox.getSelectedItem());
		
		return retVal;
	}
	
	private final AtomicReference<FindManager> findManagerRef = new AtomicReference<FindManager>();
	public FindManager getFindManager() {
		if(findManagerRef.get() == null) {
			final Session session = getEditor().getSession();
			final FindManager manager = new FindManager(session);
			manager.setAnyExpr(getAnyTierExpr());
			
			final List<TierViewItem> tierView = session.getTierView();
			for(TierViewItem tvi:tierView) {
				final SystemTierType systemTier = SystemTierType.tierFromString(tvi.getTierName());
				if(systemTier == SystemTierType.Segment) {
					continue;
				}
				manager.setExprForTier(tvi.getTierName(), exprForTier(tvi.getTierName()));
			}
			
			final List<String> findManagerTiers = new ArrayList<String>();
			final String anyExpr = searchTiers.get(ANY_TIER_NAME).getGroup(0);
			if(anyExpr.length() != 0) {
				// add all tiers to find manager
				for(TierViewItem tvi:tierView) {
					final SystemTierType systemTier = SystemTierType.tierFromString(tvi.getTierName());
					if(systemTier == SystemTierType.Segment) {
						continue;
					}
					findManagerTiers.add(tvi.getTierName());
				}
			} else {
				// only add tiers that have expressions
				for(TierViewItem tvi:tierView) {
					final FindExpr expr = exprForTier(tvi.getTierName());
					if(expr != null) {
						findManagerTiers.add(tvi.getTierName());
					}
				}
			}
			manager.setSearchTier(findManagerTiers);
			
			final SessionLocation sessionLocation = getSessionLocation();
			manager.setCurrentLocation(sessionLocation);
			
			findManagerRef.getAndSet(manager);
		}
		
		return findManagerRef.get();
	}

	/*
	 * Actions 
	 */
	public void findNext() {
		final FindManager findManager = getFindManager();
		
		final SessionRange nextInstance = findManager.findNext();
		if(nextInstance != null) {
			setupSessionSelection(nextInstance);
		} else if(findManager.getStatus() == FindStatus.HIT_END) {
			// XXX
		}
	}
	
	public void findPrev() {
		final FindManager findManager = getFindManager();
		
		final SessionRange nextInstance = findManager.findPrev();
		if(nextInstance != null) {
			setupSessionSelection(nextInstance);
		} else if(findManager.getStatus() == FindStatus.HIT_BEGINNING) {
			// XXX
		}
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
				final GroupLocation grpLocation = new GroupLocation();
				grpLocation.setGroupIndex(recordDataView.currentGroupIndex());
				grpLocation.setCharIndex(recordDataView.currentCharIndex());
				final RecordLocation recLocation = new RecordLocation(recordDataView.currentTier().getName(), grpLocation);
				retVal = new SessionLocation(recordDataView.currentRecordIndex(), recLocation);
			}
		}
		
		if(retVal == null) {
			final List<TierViewItem> tierView = getEditor().getSession().getTierView();
			
			final GroupLocation grpLocation = new GroupLocation();
			grpLocation.setGroupIndex(0);
			grpLocation.setCharIndex(0);
			final RecordLocation recordLocation = new RecordLocation(
					(tierView.size() > 0 ? tierView.get(0).getTierName() : SystemTierType.Orthography.getName()), grpLocation);
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
		}
		
		private void init() {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			
			caseSensitiveBox = new JCheckBox("Case sensitive");
			add(caseSensitiveBox);
			
			typeBox = new JComboBox(FindExpr.SearchType.values());
			typeBox.setSelectedItem(FindExpr.SearchType.PLAIN);
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
		}
		
	};
	
}

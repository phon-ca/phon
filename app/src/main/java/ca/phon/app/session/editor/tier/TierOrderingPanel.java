/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.RemoveTierEdit;
import ca.phon.app.session.editor.undo.TierNameEdit;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.session.Session;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel for changing tier ordering, visibility and fonts.
 *
 */
public class TierOrderingPanel extends EditorView {

	private final String VIEW_TITLE = "Tier Management";

	/**
	 * Tier ordering table
	 */
	private JXTable tierOrderingTable;
	
	/**
	 * New tier button
	 */
	private JButton newTierButton;
	
	/**
	 * Delete tier button 
	 */
	private JButton deleteTierButton;
	
	/**
	 * Move up button
	 */
	private JButton moveUpButton;
	
	/**
	 * Move down button
	 */
	private JButton moveDownButton;
	
	/**
	 * Font button
	 */
	private JButton editButton;
	
	/**
	 * Lock/unlock all button
	 */
	private JCheckBox lockAllBox;
	
	/**
	 * Hide/show all button
	 */
	private JCheckBox hideAllBox;
	
	/*
	 * Custom tier view order list
	 */
	private final AtomicReference<List<TierViewItem>> tierOrderRef = 
			new AtomicReference<List<TierViewItem>>(null);
	
	/**
	 * Constructor
	 */
	public TierOrderingPanel(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}
	
//	private void setupTierOrder() {
//		final SessionEditor editor = getEditor();
//		final Session session = editor.getSession();
//		
//		for(TierViewItem tierOrderItem:session.getTierView()) {
//			if(SystemTierType.isSystemTier(tierOrderItem.getTierName())) {
//				SystemTierType systemTier = SystemTierType.tierFromString(tierOrderItem.getTierName());
//				// don't add syllable, alignment and segment tiers
//				if(
//						systemTier != SystemTierType.TargetSyllables &&
//						systemTier != SystemTierType.ActualSyllables &&
//						systemTier != SystemTierType.SyllableAlignment ) {
//					tierOrder.add(tierOrderItem);
//				}
//			} else {
//				tierOrder.add(tierOrderItem);
//			}
//		}
//	}
	
	private void init() {
		final SessionEditor sessionEditor = getEditor();
		final Session session = sessionEditor.getSession();
		final TierOrderingTableModel tableModel =
				new TierOrderingTableModel(session, getCurrentOrder());
		tierOrderingTable = new JXTable(tableModel);
//		tierOrderingTable = new JXTable(new TierOrderingTableModel(getModel().getSession(), tierOrder));
		
		tierOrderingTable.setSortable(false);
		tierOrderingTable.setVisibleRowCount(5);
		tierOrderingTable.addMouseListener(new TierContextMenuListener());
		
		tierOrderingTable.getColumn(TierOrderingTableModel.TierOrderingTableColumn.GROUP_TIER.ordinal()).setCellRenderer(new DefaultTableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel retVal = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				
				Boolean val = (Boolean)value;
				
				if(val)
					retVal.setText("Yes");
				else
					retVal.setText("No");
				
				return retVal;
			}
			
		});
		
		// setup tier odering table action map
		ActionMap tierOrderActionMap = new ActionMap();
		ComponentInputMap tableInputMap = new ComponentInputMap(tierOrderingTable);
		
		final PhonUIAction deleteAction = new PhonUIAction(this, "deleteTier");
		deleteAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete the currently selected tier.");
		deleteAction.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		tierOrderActionMap.put("DELETE_TIER", deleteAction);
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_TIER");
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_TIER");
		
		tierOrderingTable.setActionMap(tierOrderActionMap);
		tierOrderingTable.setInputMap(WHEN_FOCUSED, tableInputMap);
		
		final ImageIcon addIcon = 
			IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
		final PhonUIAction addAction = new PhonUIAction(this, "newTier");
		addAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "New tier");
		addAction.putValue(PhonUIAction.SMALL_ICON, addIcon);
		newTierButton = new JButton(addAction);
		newTierButton.setFocusable(false);
		
		final ImageIcon removeIcon = 
			IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
		deleteAction.putValue(PhonUIAction.SMALL_ICON, removeIcon);
		deleteTierButton = new JButton(deleteAction);
		deleteTierButton.setFocusable(false);
		
		final ImageIcon upIcon =
			IconManager.getInstance().getIcon("actions/go-up", IconSize.XSMALL);
		final PhonUIAction upAction = new PhonUIAction(this, "moveUp");
		upAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		upAction.putValue(PhonUIAction.SMALL_ICON, upIcon);
		moveUpButton = new JButton(upAction);
		moveUpButton.setFocusable(false);
		
		final ImageIcon downIcon = 
			IconManager.getInstance().getIcon("actions/go-down", IconSize.XSMALL);
		final PhonUIAction downAction = new PhonUIAction(this, "moveDown");
		downAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		downAction.putValue(PhonUIAction.SMALL_ICON, downIcon);
		moveDownButton = new JButton(downAction);
		moveDownButton.setFocusable(false);
		
		final ImageIcon fontIcon = 
			IconManager.getInstance().getIcon("actions/edit", IconSize.XSMALL);
		final PhonUIAction fontAction = new PhonUIAction(this, "editTier");
		fontAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit tier...");
		fontAction.putValue(PhonUIAction.SMALL_ICON, fontIcon);
		editButton = new JButton(fontAction);
		editButton.setFocusable(false);
		
		final PhonUIAction lockAllAction = new PhonUIAction(this, "toggleLockAll");
		lockAllAction.putValue(PhonUIAction.NAME, "Lock all");
		lockAllBox = new JCheckBox(lockAllAction);
		lockAllBox.setSelected(false);
		
		final PhonUIAction hideAllAction = new PhonUIAction(this, "toggleShowAll");
		hideAllAction.putValue(PhonUIAction.NAME, "Hide all");
		hideAllBox = new JCheckBox(hideAllAction);
		hideAllBox.setSelected(true);
		
		FormLayout layout = new FormLayout(
				"pref, pref, fill:pref:grow, pref, pref, pref",
				"pref, pref, pref, pref, fill:pref:grow");
		CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		add(new JScrollPane(tierOrderingTable), cc.xywh(1, 2, 5, 4));
		add(moveUpButton, cc.xy(6, 2));
		add(moveDownButton, cc.xy(6, 3));
		add(editButton, cc.xy(4, 1));
		
		add(newTierButton, cc.xy(5, 1));
		
		add(lockAllBox, cc.xy(1, 1));
		add(hideAllBox, cc.xy(2, 1));
	}

//	@Override
//	public void setModel(RecordEditorModel mod) {
//		super.setModel(mod);
//		if(getModel() != null) {
//			setupTierOrder();
//			TierOrderingTableModel tableModel =
//					new TierOrderingTableModel(getModel().getSession(), tierOrder);
//			tierOrderingTable.setModel(tableModel);
//			tableModel.addTableModelListener(new TableModelListener() {
//	
//				@Override
//				public void tableChanged(TableModelEvent e) {
//	//				System.out.println(e.getType() + " " + e.getColumn() + " " +e.getFirstRow() + " " + e.getLastRow());
//					if(e.getType() == TableModelEvent.UPDATE
//							&& e.getColumn() == TierOrderingTableModel.TierOrderingTableColumn.SHOW_TIER.ordinal()) {
//						getModel().getSession().setTierView(tierOrder);
//						getModel().fireRecordEditorEvent(TIER_VIEW_CHANGED_EVT, TierOrderingPanel.this);
//					} else if(e.getType() == TableModelEvent.UPDATE
//							&& e.getColumn() == TierOrderingTableModel.TierOrderingTableColumn.LOCK_TIER.ordinal()) {
//						getModel().getSession().setTierView(tierOrder);
//						getModel().fireRecordEditorEvent(TIER_LOCK_CHANGED_EVT, TierOrderingPanel.this,
//								tierOrder.get(e.getFirstRow()).getTierName());
//					}
//				}
//	
//			});
//			
//			// TODO: handle external updates to tier ordering
//			EditorAction ee = new DelegateEditorAction(this, "onTierViewChange");
//			getModel().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, ee);
//		} else {
//			tierOrderingTable.setModel(new DefaultTableModel());
//		}
//	}
	
	private void setupEditorActions() {
		final EditorAction tierViewChangeAct = new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, tierViewChangeAct);
	}
	
	@Override
	public String getName() {
		return VIEW_TITLE;
	}
	
	/*
	 * Get current tier ordering
	 */
	private List<TierViewItem> getCurrentOrder() {
		List<TierViewItem> retVal = tierOrderRef.get();
		if(retVal == null) {
			retVal = new ArrayList<TierViewItem>(getEditor().getSession().getTierView());
			tierOrderRef.getAndSet(retVal);
		}
		return retVal;
	}
	
	private void setOrder(List<TierViewItem> tierView) {
		final List<TierViewItem> oldView = tierOrderRef.getAndSet(tierView);
		final TierViewEdit edit = new TierViewEdit(getEditor(), oldView, tierView);
		getEditor().getUndoSupport().postEdit(edit);
	}
	
	/**
	 * Toggle lock all tiers
	 */
	public void toggleLockAll() {
		boolean lock = lockAllBox.getText().equals("Lock all");
		
		final SessionFactory factory = SessionFactory.newFactory();
		final List<TierViewItem> currentView = getCurrentOrder();
		final List<TierViewItem> newTierView = new ArrayList<>();
		for(TierViewItem toi:currentView) {
			final TierViewItem newItem = 
					factory.createTierViewItem(toi.getTierName(), toi.isVisible(), toi.getTierFont(), lock);
		}
		setOrder(newTierView);
		
//		getModel().getSession().setTierView(tierOrder);
//		
//		final TierOrderingTableModel tableModel = (TierOrderingTableModel)tierOrderingTable.getModel();
//		for(int i = 0; i < tierOrder.size(); i++) {
//			final ITierOrderItem toi = tierOrder.get(i);
//			getModel().fireRecordEditorEvent(TIER_LOCK_CHANGED_EVT, TierOrderingPanel.this,
//					toi.getTierName());
//			tableModel.fireTableCellUpdated(i, TierOrderingTableModel.TierOrderingTableColumn.LOCK_TIER.ordinal());
//		}
		
		lockAllBox.setText(lock ? "Unlock all" : "Lock all");
	}
	
	/**
	 * Toggle show all tiers
	 */
	public void toggleShowAll() {
		boolean hide = hideAllBox.getText().equals("Hide all");
		
		final SessionFactory factory = SessionFactory.newFactory();
		final List<TierViewItem> currentView = getCurrentOrder();
		final List<TierViewItem> newTierView = new ArrayList<>();
		for(TierViewItem toi:currentView) {
			final TierViewItem newItem = 
					factory.createTierViewItem(toi.getTierName(), !hide, toi.getTierFont(), toi.isTierLocked());
		}
		setOrder(newTierView);
		
//		getModel().getSession().setTierView(tierOrder);
//		
//		final TierOrderingTableModel tableModel = (TierOrderingTableModel)tierOrderingTable.getModel();
//		for(int i = 0; i < tierOrder.size(); i++) {
//			tableModel.fireTableCellUpdated(i, TierOrderingTableModel.TierOrderingTableColumn.SHOW_TIER.ordinal());
//		}
//		getModel().fireRecordEditorEvent(TIER_VIEW_CHANGED_EVT, TierOrderingPanel.this);
		
		hideAllBox.setText(hide ? "Show all" : "Hide all");
	}
	
	/**
	 * Move selected tier up in order
	 */
	public void moveUp() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		final List<TierViewItem> newOrder = new ArrayList<>(tierOrder);
		if(selectedRow > 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			newOrder.remove(tierItem);
			newOrder.add(selectedRow-1, tierItem);
			
			setOrder(newOrder);
		}
	}
	
	/**
	 * Move selected tier down in order
	 */
	public void moveDown() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		final List<TierViewItem> newOrder = new ArrayList<>(tierOrder);
		if(selectedRow >= 0 && selectedRow < tierOrder.size()-1) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			newOrder.remove(tierItem);
			newOrder.add(selectedRow+1, tierItem);
			
			setOrder(newOrder);
		}
	}
	
	public void onEditTier(PhonActionEvent pae) {
		editTier();
	}
	
	/**
	 * Edit the currently selected tier
	 */
	public void editTier() {
		final SessionFactory factory = SessionFactory.newFactory();
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			final SystemTierType systemTierType = SystemTierType.tierFromString(tierItem.getTierName());
			
			TierDescription depTierDesc  = null;
			if(systemTierType != null) {
				depTierDesc = factory.createTierDescription(systemTierType.getName(), systemTierType.isGrouped());
			} else {
				final SessionEditor editor = getEditor();
				final Session session = editor.getSession();
				
				for(int i = 0; i < session.getUserTierCount(); i++) {
					final TierDescription td = session.getUserTier(i);
					if(td.getName().equals(tierItem.getTierName())) {
						depTierDesc = td;
						break;
					}
				}
			}
			
			if(depTierDesc != null) {
				final Font transcriptFont = 
						(tierItem.getTierFont().equals("default") ? 
								PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT)) :
									Font.decode(tierItem.getTierFont()));

				TierEditorDialog tierDialog = new TierEditorDialog(true);
				TierEditor tierEditor = tierDialog.getTierEditor();
				tierEditor.setGrouped(depTierDesc.isGrouped());
				tierEditor.setTierName(tierItem.getTierName());
				tierEditor.setTierFont(transcriptFont);
				
				tierDialog.add(tierEditor);
				tierDialog.setTitle("New Tier");
				tierDialog.setModal(true);
				tierDialog.pack();
				
				if(tierDialog.showDialog()) {
					// change of tier name
					if(!depTierDesc.getName().equals(tierEditor.getTierName())) {
						String oldTierName = depTierDesc.getName();
						
						final TierNameEdit edit = new TierNameEdit(getEditor(), tierEditor.getTierName(), oldTierName);
						getEditor().getUndoSupport().postEdit(edit);
					}
					
					
					// change font
//					if(!tierItem.getTierFont().equals(StringUtils.fontToString(tierEditor.getTierFont()))) {
//						final String test = StringUtils.fontToString(UserPrefManager.getTranscriptFont());
//						String fontString = StringUtils.fontToString(tierEditor.getTierFont());
//						if(fontString.equals(test))
//							fontString = "default";
//						tierItem.setTierFont(fontString);
//					}
//					
//					((TierOrderingTableModel)tierOrderingTable.getModel()).fireTableRowsUpdated(selectedRow, selectedRow);
//					
//					getModel().getSession().setTierView(tierOrder);
//					getModel().fireRecordEditorEvent(TIER_VIEW_CHANGED_EVT, this);
				} // if (showDialog())
			} // if (depTierDesc != null)
		} // if (selectedRow >= 0)
	}
	
	/**
	 * Show the new tier dialog
	 */
	public void newTier() {
//		TierEditor tierEditor = new TierEditor();
		TierEditorDialog newTierDialog = new TierEditorDialog(false);
		TierEditor tierEditor = newTierDialog.getTierEditor();
		newTierDialog.add(tierEditor);
		newTierDialog.setTitle("New Tier");
		newTierDialog.setModal(true);
		newTierDialog.pack();
		
		if(newTierDialog.showDialog()) {
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			// get tier info
			String tierName = tierEditor.getTierName();
			tierName = StringUtils.strip(tierName);
			if(tierName.length() == 0) {
				return;
			}
			
			boolean tierExists = false;
			if(SystemTierType.isSystemTier(tierName)) {
				tierExists = true;
			} else {
				for(TierDescription td:session.getUserTiers()) {
					if(td.getName().equals(tierName)) {
						tierExists = true;
						break;
					}
				}
			}
			
			if(tierExists){
				final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
				toast.start(tierEditor);
				return;
			}
			
			// create tier
			final TierDescription tierDescription = tierEditor.createTierDescription();
			final TierViewItem tierViewItem = tierEditor.createTierViewItem();
			
			final AddTierEdit edit = new AddTierEdit(editor, tierDescription, tierViewItem);
			editor.getUndoSupport().postEdit(edit);
		}
	}
	
	public void onDeleteTier(PhonActionEvent pae) {
		deleteTier();
	}
	
	/**
	 * Delete the currently selected tier
	 */
	public void deleteTier() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		final List<TierViewItem> tierOrder = getCurrentOrder();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			if(!SystemTierType.isSystemTier(tierItem.getTierName())) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(getEditor());
				props.setOptions(MessageDialogProperties.okCancelOptions);
				props.setRunAsync(false);
				props.setTitle("Delete tier");
				props.setHeader("Delete tier?");
				props.setMessage("Delete tier '" + tierItem.getTierName() + "'? This action cannot be undone.");
				
				int retVal = NativeDialogs.showMessageDialog(props);
				if(retVal == 0) {
					TierDescription tierDesc = null;
					for(TierDescription td:session.getUserTiers()) {
						if(td.getName().equals(tierItem.getTierName())) {
							tierDesc = td;
							break;
						}
					}
					
					final RemoveTierEdit edit = new RemoveTierEdit(editor, tierDesc, tierItem);
					editor.getUndoSupport().postEdit(edit);
				}
			}
		}
	}
	
	public void onResetTierFont(PhonActionEvent pae) {
//		int row = (Integer)pae.getData();
//		
//		tierOrder.get(row).setTierFont("default");
//		
//		getModel().getSession().setTierView(tierOrder);
//		getModel().fireRecordEditorEvent(TIER_VIEW_CHANGED_EVT, this);
	}
	
	/*
	 * Editor events 
	 */
	@RunOnEDT
	public void onTierViewChange(EditorEvent ee) {
//		if(ee.getSource() != getEditor().get) {
			final List<TierViewItem> tierOrder = new ArrayList<>(getEditor().getSession().getTierView());
			tierOrderRef.getAndSet(tierOrder);
			
			// check for a current selection
			((TierOrderingTableModel)tierOrderingTable.getModel()).setTierView(tierOrder);
//		}
	}

	/**
	 * Listener for tier context menu
	 */
	private class TierContextMenuListener extends MouseInputAdapter {

		@Override
		public void mousePressed(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				showContextMenu(arg0);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if(arg0.isPopupTrigger()) {
				showContextMenu(arg0);
			}
		}
		
		private void showContextMenu(MouseEvent arg0) {
			// get the selected index
			TierOrderingTableModel tblModel = 
				(TierOrderingTableModel)tierOrderingTable.getModel();
			int row = tierOrderingTable.rowAtPoint(arg0.getPoint());
			final List<TierViewItem> tierOrder = getCurrentOrder();
			if(row >= 0 && row < tierOrder.size()) {
				tierOrderingTable.getSelectionModel().setSelectionInterval(row, row);
				JPopupMenu popupMenu = new JPopupMenu();
				
				PhonUIAction resetFontAction = 
					new PhonUIAction(TierOrderingPanel.this, "onResetTierFont", row);
				resetFontAction.putValue(Action.NAME, "Reset tier font");
				resetFontAction.putValue(Action.SHORT_DESCRIPTION, "Reset tier font to default");
				JMenuItem resetItem = new JMenuItem(resetFontAction);
				popupMenu.add(resetItem);
				
				PhonUIAction editTierAction = 
					new PhonUIAction(TierOrderingPanel.this, "onEditTier", row);
				editTierAction.putValue(Action.NAME, "Edit tier...");
				editTierAction.putValue(Action.SHORT_DESCRIPTION, "Edit tier properties");
				JMenuItem editTierItem = new JMenuItem(editTierAction);
				popupMenu.add(editTierItem);
				
				PhonUIAction deleteTierAction =
					new PhonUIAction(TierOrderingPanel.this, "onDeleteTier", row);
				deleteTierAction.putValue(Action.NAME, "Delete tier");
				deleteTierAction.putValue(Action.SHORT_DESCRIPTION, "Delete tier from session");
				deleteTierAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
				JMenuItem deleteTierItem = new JMenuItem(deleteTierAction);
				popupMenu.add(deleteTierItem);
				
				popupMenu.show(tierOrderingTable, arg0.getPoint().x, arg0.getPoint().y);
			} 
		}
		
	}
	
	/**
	 * Simple dialog that closes on OK or Cancel.  Use showDialog() to display the
	 * dialog and get the return value.
	 */
	private class TierEditorDialog extends JDialog {
		
		private static final long serialVersionUID = 1218564949424490169L;

		private DialogHeader header;
		
		private TierEditor tierEditor;
		
		private JButton okButton;
		
		private JButton cancelButton;
		
		private boolean okPressed = false;
		
		public TierEditorDialog(boolean editMode) {
			super();
			
			if(editMode)
				super.setTitle("Edit Tier");
			else
				super.setTitle("New Tier");
			
			super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

			tierEditor = new TierEditor(editMode);
			
			init();
		}
		
		private void init() {
			header = new DialogHeader(getTitle(), "");
			
			
			okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					okPressed = true;
					TierEditorDialog.this.setVisible(false);
				}
				
			});
			
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					okPressed = false;
					TierEditorDialog.this.setVisible(false);
				}
				
			});
			
			JPanel btnPanel = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
			
			BorderLayout layout = new BorderLayout();
			setLayout(layout);
			
			add(header, BorderLayout.NORTH);
			add(tierEditor, BorderLayout.CENTER);
			add(btnPanel, BorderLayout.SOUTH);
			
			getRootPane().setDefaultButton(okButton);
		}
		
		public TierEditor getTierEditor() {
			return tierEditor;
		}
		
		/**
		 * Displays dialog to user, closes when either button is
		 * pressed.
		 * 
		 * @return true if ok was pressed, false otherwise
		 */
		public boolean showDialog() {
			pack();
			Dimension size = getSize();
			
			// center dialog on screen
			Dimension ss = 
				Toolkit.getDefaultToolkit().getScreenSize();
			
			if(size.width == 0 && size.height == 0)
				size = getPreferredSize();
			
			int xPos = ss.width / 2 - (size.width/2);
			int yPos = ss.height / 2 - (size.height/2);
			
			setBounds(xPos, yPos, size.width, size.height);
			
			setVisible(true);
			
			// .. wait for dialog
			
			return okPressed;
		}
		
		/**
		 * If not modal, showDialog will always return false.
		 * Use this method to get the dialog result.
		 * 
		 * @return
		 */
		public boolean wasOkPressed() {
			return okPressed;
		}
	}

	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/record-settings", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

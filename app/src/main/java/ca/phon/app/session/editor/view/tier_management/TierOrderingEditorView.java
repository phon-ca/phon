/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.tier_management;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.EditorView;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.TierViewEdit;
import ca.phon.app.session.editor.view.tier_management.actions.EditTierAction;
import ca.phon.app.session.editor.view.tier_management.actions.MoveTierAction;
import ca.phon.app.session.editor.view.tier_management.actions.NewTierAction;
import ca.phon.app.session.editor.view.tier_management.actions.RemoveTierAction;
import ca.phon.app.session.editor.view.tier_management.actions.ResetTierFontAction;
import ca.phon.app.session.editor.view.tier_management.actions.ToggleHideAllTiersAction;
import ca.phon.app.session.editor.view.tier_management.actions.ToggleLockAllTiersAction;
import ca.phon.app.session.editor.view.tier_management.actions.ToggleTierLockAction;
import ca.phon.app.session.editor.view.tier_management.actions.ToggleTierVisibleAction;
import ca.phon.session.Session;
import ca.phon.session.TierDescription;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Panel for changing tier ordering, visibility and fonts.
 *
 */
public class TierOrderingEditorView extends EditorView {

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
	
	/*
	 * Custom tier view order list
	 */
	private final AtomicReference<List<TierViewItem>> tierOrderRef = 
			new AtomicReference<List<TierViewItem>>(null);
	
	/**
	 * Constructor
	 */
	public TierOrderingEditorView(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}
	
	private void toggleTierVisible(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];
		
		final ToggleTierVisibleAction act = new ToggleTierVisibleAction(getEditor(), this, tv);
		act.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	private void toggleTierLocked(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];
		
		final ToggleTierLockAction act = new ToggleTierLockAction(getEditor(), this, tv);
		act.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	private void init() {
		final SessionEditor sessionEditor = getEditor();
		final Session session = sessionEditor.getSession();
		final TierOrderingTableModel tableModel =
				new TierOrderingTableModel(session, getCurrentOrder()) {

					private static final long serialVersionUID = 1L;

					@Override
					public void setValueAt(Object aValue, int rowIndex,
							int columnIndex) {
						super.setValueAt(aValue, rowIndex, columnIndex);
						if(columnIndex == TierOrderingTableModel.TierOrderingTableColumn.SHOW_TIER.ordinal()) {
							toggleTierVisible(rowIndex);
						} else if(columnIndex == TierOrderingTableModel.TierOrderingTableColumn.LOCK_TIER.ordinal()) {
							toggleTierLocked(rowIndex);
						}
					}
			
		};
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
		
		final PhonUIAction deleteAction = new PhonUIAction(this, "onDeleteTier");
		deleteAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete the currently selected tier.");
		deleteAction.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		tierOrderActionMap.put("DELETE_TIER", deleteAction);
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_TIER");
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_TIER");
		
		tierOrderingTable.setActionMap(tierOrderActionMap);
		tierOrderingTable.setInputMap(WHEN_FOCUSED, tableInputMap);
		
		final NewTierAction addAction = new NewTierAction(getEditor(), this);
		newTierButton = new JButton(addAction);
		newTierButton.setFocusable(false);
		
		final ImageIcon removeIcon = 
			IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
		deleteAction.putValue(PhonUIAction.SMALL_ICON, removeIcon);
		deleteTierButton = new JButton(deleteAction);
		deleteTierButton.setFocusable(false);
		
		final ImageIcon upIcon =
			IconManager.getInstance().getIcon("actions/go-up", IconSize.SMALL);
		final PhonUIAction upAction = new PhonUIAction(this, "moveUp");
		upAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		upAction.putValue(PhonUIAction.SMALL_ICON, upIcon);
		moveUpButton = new JButton(upAction);
		moveUpButton.setFocusable(false);
		
		final ImageIcon downIcon = 
			IconManager.getInstance().getIcon("actions/go-down", IconSize.SMALL);
		final PhonUIAction downAction = new PhonUIAction(this, "moveDown");
		downAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		downAction.putValue(PhonUIAction.SMALL_ICON, downIcon);
		moveDownButton = new JButton(downAction);
		moveDownButton.setFocusable(false);
		
		final ImageIcon fontIcon = 
			IconManager.getInstance().getIcon("actions/edit", IconSize.SMALL);
		final PhonUIAction fontAction = new PhonUIAction(this, "onEditTier");
		fontAction.putValue(PhonUIAction.NAME, "Edit tier...");
		fontAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit tier...");
		fontAction.putValue(PhonUIAction.SMALL_ICON, fontIcon);
		editButton = new JButton(fontAction);
		editButton.setFocusable(false);
		
		final ToggleLockAllTiersAction lockAllAction = new ToggleLockAllTiersAction(getEditor(), this);
		final JButton lockAllButton = new JButton(lockAllAction);
		
		final ToggleHideAllTiersAction hideAllAction = new ToggleHideAllTiersAction(getEditor(), this);
		final JButton hideAllButton = new JButton(hideAllAction);
		
		FormLayout layout = new FormLayout(
				"pref, pref, fill:pref:grow, pref, pref, pref",
				"pref, pref, pref, pref, fill:pref:grow");
		CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		add(new JScrollPane(tierOrderingTable), cc.xywh(1, 2, 5, 4));
		add(moveUpButton, cc.xy(6, 2));
		add(moveDownButton, cc.xy(6, 3));
		add(editButton, cc.xy(5, 1));
		
		add(newTierButton, cc.xy(4, 1));
		
		add(lockAllButton, cc.xy(1, 1));
		add(hideAllButton, cc.xy(2, 1));
	}
	
	private void setupEditorActions() {
		final EditorAction tierViewChangeAct = new DelegateEditorAction(this, "onTierViewChange");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, tierViewChangeAct);
	}
	
	public JTable getTierOrderingTable() {
		return this.tierOrderingTable;
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
	 * Move selected tier up in order
	 */
	public void moveUp() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		if(selectedRow > 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			final MoveTierAction act = new MoveTierAction(getEditor(), this, tierItem, -1);
			act.actionPerformed(new ActionEvent(this, 0, null));
		}
	}
	
	/**
	 * Move selected tier down in order
	 */
	public void moveDown() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		if(selectedRow >= 0 && selectedRow < tierOrder.size()-1) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			final MoveTierAction act = new MoveTierAction(getEditor(), this, tierItem, 1);
			act.actionPerformed(new ActionEvent(this, 0, null));
		}
	}
	
	public void onEditTier(PhonActionEvent pae) {
		int selectedRow = tierOrderingTable.getSelectedRow();
		final List<TierViewItem> tierOrder = getCurrentOrder();
		
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			final EditTierAction act = new EditTierAction(getEditor(), this, tierItem);
			act.actionPerformed(pae.getActionEvent());
		}
	}
	
	public void onDeleteTier(PhonActionEvent pae) {
		int selectedRow = tierOrderingTable.getSelectedRow();
		final List<TierViewItem> tierOrder = getCurrentOrder();
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			TierDescription tierDesc = null;
			for(TierDescription td:session.getUserTiers()) {
				if(td.getName().equals(tierItem.getTierName())) {
					tierDesc = td;
					break;
				}
			}
			
			final RemoveTierAction act = new RemoveTierAction(getEditor(), this, tierDesc, tierItem);
			act.actionPerformed(pae.getActionEvent());
		}
	}
	
	/*
	 * Editor events 
	 */
	@RunOnEDT
	public void onTierViewChange(EditorEvent ee) {
//		if(ee.getSource() != getEditor().get) {
			final List<TierViewItem> tierOrder = new ArrayList<TierViewItem>(getEditor().getSession().getTierView());
			tierOrderRef.getAndSet(tierOrder);
			
			// check for a current selection
			int selectedRow = getTierOrderingTable().getSelectedRow();
			((TierOrderingTableModel)tierOrderingTable.getModel()).setTierView(tierOrder);
			if(selectedRow >= 0 && selectedRow < tierOrderingTable.getRowCount()) {
				tierOrderingTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
			}
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
		
		@Override
		public void mouseClicked(MouseEvent me) {
			if(me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
				onEditTier(new PhonActionEvent(null));
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
				
				final ResetTierFontAction resetFontAction = new ResetTierFontAction(getEditor(), 
						TierOrderingEditorView.this, tierOrder.get(row));
				JMenuItem resetItem = new JMenuItem(resetFontAction);
				popupMenu.add(resetItem);
				
				final EditTierAction editTierAction = new EditTierAction(getEditor(), TierOrderingEditorView.this, tierOrder.get(row));
				JMenuItem editTierItem = new JMenuItem(editTierAction);
				popupMenu.add(editTierItem);
				
				TierDescription td = null;
				for(TierDescription t:getEditor().getSession().getUserTiers()) {
					if(t.getName().equals(tierOrder.get(row).getTierName())) {
						td = t;
						break;
					}
				}
				
				if(td != null) {
					final RemoveTierAction deleteTierAction = new RemoveTierAction(getEditor(), TierOrderingEditorView.this, td, tierOrder.get(row));
					deleteTierAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
					JMenuItem deleteTierItem = new JMenuItem(deleteTierAction);
					popupMenu.add(deleteTierItem);
				}
				
				popupMenu.show(tierOrderingTable, arg0.getPoint().x, arg0.getPoint().y);
			} 
		}
		
	}
	
	@Override
	public ImageIcon getIcon() {
		return IconManager.getInstance().getIcon("misc/record-settings", IconSize.SMALL);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		
		retVal.add(new ToggleLockAllTiersAction(getEditor(), this));
		retVal.add(new ToggleHideAllTiersAction(getEditor(), this));
		retVal.addSeparator();
		
		final List<TierViewItem> view = getCurrentOrder();
		for(int i = 0; i < view.size(); i++) {
			final TierViewItem tvi = view.get(i);
			final JMenu tierMenu = new JMenu(tvi.getTierName());
			
			TierDescription tierDesc = null;
			for(TierDescription td:getEditor().getSession().getUserTiers()) {
				if(td.getName().equals(tvi.getTierName())) {
					tierDesc = td;
					break;
				}
			}
			
			final MoveTierAction moveUpAction = new MoveTierAction(getEditor(), this, tvi, -1);
			final MoveTierAction moveDownAction = new MoveTierAction(getEditor(), this, tvi, 1);
			if(i > 0)
				tierMenu.add(moveUpAction);
			if(i < view.size() - 1)
				tierMenu.add(moveDownAction);
			tierMenu.addSeparator();
			tierMenu.add(new ToggleTierLockAction(getEditor(), this, tvi));
			tierMenu.add(new ToggleTierVisibleAction(getEditor(), this, tvi));
			tierMenu.addSeparator();
			tierMenu.add(new EditTierAction(getEditor(), this, tvi));
			tierMenu.add(new ResetTierFontAction(getEditor(), this, tvi));
			if(tierDesc != null)
				tierMenu.add(new RemoveTierAction(getEditor(), this, tierDesc, tvi));
		
			retVal.add(tierMenu);
		}
		retVal.add(new NewTierAction(getEditor(), this));
		
		return retVal;
	}
	
	
}

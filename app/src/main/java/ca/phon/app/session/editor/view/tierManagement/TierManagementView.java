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
package ca.phon.app.session.editor.view.tierManagement;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.actions.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.icons.*;
import org.jdesktop.swingx.JXTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Panel for changing tier ordering, visibility and fonts.
 *
 */
public class TierManagementView extends EditorView {

	public final static String VIEW_NAME = "Tier Management";

	public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":data_table";

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
	public TierManagementView(SessionEditor editor) {
		super(editor);
		init();
		setupEditorActions();
	}
	
	private void toggleTierVisible(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];
		
		final ToggleTierVisibleAction act = new ToggleTierVisibleAction(getEditor(), tv);
		act.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	private void toggleTierLocked(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];
		
		final ToggleTierLockAction act = new ToggleTierLockAction(getEditor(), tv);
		act.actionPerformed(new ActionEvent(this, 0, null));
	}

	private void toggleTierBlind(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];

		final ToggleTierBlind act = new ToggleTierBlind(getEditor(), tv.getTierName());
		act.actionPerformed(new ActionEvent(this, 0, null));
	}

	private void toggleTierAligned(int rowIndex) {
		final TierViewItem[] tierView = getCurrentOrder().toArray(new TierViewItem[0]);
		final TierViewItem tv = tierView[rowIndex];

//		final ToggleTierAlignedAction act = new ToggleTierAlignedAction(getEditor(), tv);
//		act.actionPerformed(new ActionEvent(this, 0, null));
	}
	
	private void init() {
		final SessionEditor sessionEditor = getEditor();
		final Session session = sessionEditor.getSession();
		final TierOrderingTableModel tableModel =
				new TierOrderingTableModel(session, getCurrentOrder()) {

					@Override
					public void setValueAt(Object aValue, int rowIndex,
							int columnIndex) {
						super.setValueAt(aValue, rowIndex, columnIndex);
						if(columnIndex == TierOrderingTableModel.TierOrderingTableColumn.SHOW_TIER.ordinal()) {
							toggleTierVisible(rowIndex);
						} else if(columnIndex == TierOrderingTableModel.TierOrderingTableColumn.LOCK_TIER.ordinal()) {
							toggleTierLocked(rowIndex);
						} else if(columnIndex == TierOrderingTableColumn.BLIND.ordinal()) {
							toggleTierBlind(rowIndex);
						} else if(columnIndex == TierOrderingTableColumn.ALIGNED.ordinal()) {
							toggleTierAligned(rowIndex);
						}
					}
			
		};
		tierOrderingTable = new PhonTable(tableModel);

		tierOrderingTable.setSortable(false);
		tierOrderingTable.setFocusable(true);
		tierOrderingTable.setColumnControlVisible(true);
		tierOrderingTable.setVisibleRowCount(5);
		tierOrderingTable.addMouseListener(new TierContextMenuListener());
		tierOrderingTable.setDragEnabled(true);
		tierOrderingTable.setTransferHandler(new TierTableTransferHandler());
		tierOrderingTable.setDropMode(DropMode.INSERT_ROWS);
		tierOrderingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tierOrderingTable.getColumn(0).setMaxWidth(75);
		tierOrderingTable.getColumn(1).setMaxWidth(75);

		for(int i = tierOrderingTable.getColumnCount()-1; i >= 0; i--) {
			tierOrderingTable.getColumnExt(i).setVisible(i < 5);
		}

		// setup tier odering table action map
		ActionMap tierOrderActionMap = tierOrderingTable.getActionMap();
		InputMap tableInputMap = tierOrderingTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		final PhonUIAction<Void> deleteAction = PhonUIAction.eventConsumer(this::onDeleteTier);
		deleteAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete the currently selected tier.");
		deleteAction.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		tierOrderActionMap.put("DELETE_TIER", deleteAction);
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_TIER");
		tableInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "DELETE_TIER");
		
		tierOrderingTable.setActionMap(tierOrderActionMap);
		tierOrderingTable.setInputMap(WHEN_FOCUSED, tableInputMap);
		
		final PhonUIAction<Void> addAction = PhonUIAction.runnable(this::onNewTier);
		addAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		addAction.putValue(FlatButton.ICON_NAME_PROP, "add");
		addAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		newTierButton = new FlatButton(addAction);
		newTierButton.setText(null);
		newTierButton.setFocusable(false);
		((FlatButton)newTierButton).setPadding(2);

		deleteAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		deleteAction.putValue(FlatButton.ICON_NAME_PROP, "remove");
		deleteAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		deleteTierButton = new FlatButton(deleteAction);
		deleteTierButton.setFocusable(false);
		((FlatButton)deleteTierButton).setPadding(2);
		
		final PhonUIAction<Void> upAction = PhonUIAction.runnable(this::moveUp);
		upAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		upAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_upward");
		upAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		upAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		moveUpButton = new FlatButton(upAction);
		moveUpButton.setFocusable(false);
		((FlatButton)moveUpButton).setPadding(2);
		
		final PhonUIAction<Void> downAction = PhonUIAction.runnable(this::moveDown);
		downAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		downAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_downward");
		downAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		downAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		moveDownButton = new FlatButton(downAction);
		moveDownButton.setFocusable(false);
		((FlatButton)moveDownButton).setPadding(2);
		
		final PhonUIAction<Void> editAction = PhonUIAction.eventConsumer(this::onEditTier);
		editAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit tier...");
		editAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		editAction.putValue(FlatButton.ICON_NAME_PROP, "edit");
		editAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		editButton = new FlatButton(editAction);
		editButton.setFocusable(false);
		((FlatButton)editButton).setPadding(2);
		
		setLayout(new BorderLayout());
		add(new JScrollPane(tierOrderingTable), BorderLayout.CENTER);

		final IconStrip iconStrip = new IconStrip(SwingConstants.HORIZONTAL);
		iconStrip.add(newTierButton, IconStrip.IconStripPosition.LEFT);
		iconStrip.add(deleteTierButton, IconStrip.IconStripPosition.LEFT);
		iconStrip.add(editButton, IconStrip.IconStripPosition.LEFT);
		iconStrip.add(moveUpButton, IconStrip.IconStripPosition.RIGHT);
		iconStrip.add(moveDownButton, IconStrip.IconStripPosition.RIGHT);
		add(iconStrip, BorderLayout.NORTH);
	}
	
	private void setupEditorActions() {
		getEditor().getEventManager().registerActionForEvent(EditorEventType.SessionChanged, this::onSessionChanged, EditorEventManager.RunOn.AWTEventDispatchThread);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TierViewChanged, this::onTierViewChange, EditorEventManager.RunOn.AWTEventDispatchThread);
	}
	
	public JTable getTierOrderingTable() {
		return this.tierOrderingTable;
	}
	
	@Override
	public String getName() {
		return VIEW_NAME;
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

	public void setupTierContextMenu(int tierViewIdx, MenuBuilder builder) {
		List<TierViewItem> tierView = getCurrentOrder();
		if(tierViewIdx >= tierView.size()) return;
		TierViewItem tvi = tierView.get(tierViewIdx);
		final TierDescription tierDesc = getEditor().getSession().getUserTier(tvi.getTierName());
		TierMenuBuilder.setupTierMenu(getEditor(), tierDesc, tvi, builder);
	}

	public void onNewTier() {
		final JMenu newTierMenu = new JMenu();
		final MenuBuilder builder = new MenuBuilder(newTierMenu);
		TierMenuBuilder.setupNewTierMenu(getEditor(), builder);
		// show popup menu on new tier button
		newTierMenu.getPopupMenu().show(newTierButton, 0, newTierButton.getHeight());
	}

	public void onSelectFont(PhonActionEvent<TierViewItem> pae) {
		if(pae.getData() ==  null) return;
		TierViewItem tvi = pae.getData();
		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));

		final FontDialogProperties props = new FontDialogProperties();
		props.setRunAsync(true);
		props.setListener(new FontDlgListener(tvi));
		props.setFontName(currentFont.getName());
		props.setFontSize(currentFont.getSize());
		props.setBold(currentFont.isBold());
		props.setItalic(currentFont.isItalic());
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());

		NativeDialogs.showFontDialog(props);
	}

	private class FontDlgListener implements NativeDialogListener {

		private TierViewItem tvi;

		public FontDlgListener(TierViewItem tvi) {
			this.tvi = tvi;
		}

		@Override
		public void nativeDialogEvent(NativeDialogEvent nativeDialogEvent) {
			if(nativeDialogEvent.getDialogResult() == NativeDialogEvent.OK_OPTION) {
				Font selectedFont = (Font)nativeDialogEvent.getDialogData();

				TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), tvi.isVisible(),
						(new FontFormatter()).format(selectedFont), tvi.isTierLocked());

				final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), tvi, newItem);
				getEditor().getUndoSupport().postEdit(edit);
			}
		}
	};

	/**
	 * Move selected tier up in order
	 */
	public void moveUp() {
		int selectedRow = tierOrderingTable.getSelectedRow();
		
		final List<TierViewItem> tierOrder = getCurrentOrder();
		if(selectedRow > 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			
			final MoveTierAction act = new MoveTierAction(getEditor(), tierItem, -1);
			act.actionPerformed(new ActionEvent(this, 0, null));
			tierOrderingTable.getSelectionModel().setSelectionInterval(selectedRow-1, selectedRow-1);
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
			
			final MoveTierAction act = new MoveTierAction(getEditor(), tierItem, 1);
			act.actionPerformed(new ActionEvent(this, 0, null));
			tierOrderingTable.getSelectionModel().setSelectionInterval(selectedRow+1, selectedRow+1);
		}
	}
	
	public void onEditTier(PhonActionEvent<Void> pae) {
		int selectedRow = tierOrderingTable.getSelectedRow();
		final List<TierViewItem> tierOrder = getCurrentOrder();
		
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			final EditTierAction act = new EditTierAction(getEditor(), tierItem);
			act.actionPerformed(pae.getActionEvent());
		}
	}
	
	public void onDeleteTier(PhonActionEvent<Void> pae) {
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
			
			if(tierDesc != null) {
				final TierDescription td = tierDesc;
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(CommonModuleFrame.getCurrentFrame());
				props.setHeader("Delete Tier");
				props.setMessage("Delete tier " + td.getName() + "?");
				props.setRunAsync(true);
				props.setListener( (e) -> {
					if(e.getDialogResult() == 0) {
						final RemoveTierAction act = new RemoveTierAction(getEditor(), td, tierItem);
						act.actionPerformed(pae.getActionEvent());
					}
				});
				props.setOptions(MessageDialogProperties.okCancelOptions);
				NativeDialogs.showMessageDialog(props);
			} else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
	
	/*
	 * Editor events 
	 */
	private void onSessionChanged(EditorEvent<Session> ee) {
		onDataChange(ee);
	}

	public void onTierViewChange(EditorEvent<EditorEventType.TierViewChangedData> ee) {
		onDataChange(ee);
	}

	private void onDataChange(EditorEvent<?> ee) {
		final List<TierViewItem> tierOrder = new ArrayList<TierViewItem>(getEditor().getSession().getTierView());
		tierOrderRef.getAndSet(tierOrder);

		// check for a current selection
		int selectedRow = getTierOrderingTable().getSelectedRow();
		((TierOrderingTableModel)tierOrderingTable.getModel()).setTierView(tierOrder);
		if(selectedRow >= 0 && selectedRow < tierOrderingTable.getRowCount()) {
			tierOrderingTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
		}
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
				MenuBuilder builder = new MenuBuilder(popupMenu);

				setupTierContextMenu(row, builder);

				popupMenu.show(tierOrderingTable, arg0.getPoint().x, arg0.getPoint().y);
			} 
		}
		
	}
	
	@Override
	public ImageIcon getIcon() {
		final String[] iconData = VIEW_ICON.split(":");
		return IconManager.getInstance().getFontIcon(iconData[0], iconData[1], IconSize.MEDIUM, Color.darkGray);
	}

	@Override
	public JMenu getMenu() {
		final JMenu retVal = new JMenu();
		final MenuBuilder menuBuilder = new MenuBuilder(retVal);
		final JMenu newTierMenu = menuBuilder.addMenu(".", "Add tier");
		newTierMenu.setIcon(IconManager.getInstance().getFontIcon("add", IconSize.SMALL, UIManager.getColor("Button.foreground")));
		TierMenuBuilder.setupNewTierMenu(getEditor(), new MenuBuilder(newTierMenu));
		menuBuilder.addSeparator(".", "existing_tiers");
		// add existing tier menus
		TierMenuBuilder.appendExistingTiersMenu(getEditor(), menuBuilder);
		menuBuilder.addSeparator(".", "lock_hide");
		retVal.add(new ToggleLockAllTiersAction(getEditor()));
		retVal.add(new ToggleHideAllTiersAction(getEditor()));
		return retVal;
	}

	private record TierViewDragData(int originalRow, TierViewItem tvi) {}

	private final class TierTableTransferHandler extends TransferHandler {

		final static DataFlavor dataFlavor = new DataFlavor(TierViewDragData.class, "Tier name");

		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDataFlavorSupported(dataFlavor);
		}

		@Override
		public boolean importData(TransferSupport support) {
			if(!canImport(support)) return false;

			final JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();
			int row = dropLocation.getRow();

			TierViewItem item = null;
			try {
				TierViewDragData data = (TierViewDragData) support.getTransferable().getTransferData(dataFlavor);
				item = data.tvi();
				if(data.originalRow() < row) {
					row--;
				}
			} catch (UnsupportedFlavorException | IOException e) {
				return false;
			}

			final MoveTierEdit edit = new MoveTierEdit(getEditor(), item, row);
			getEditor().getUndoSupport().postEdit(edit);

			tierOrderingTable.getSelectionModel().setSelectionInterval(row, row);

			return true;
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY_OR_MOVE;
		}

		@Nullable
		@Override
		protected Transferable createTransferable(JComponent c) {
			if(c != tierOrderingTable) {
				return null;
			}

			final int selectedRow = tierOrderingTable.getSelectedRow();
			if(selectedRow >= 0 && selectedRow < tierOrderingTable.getRowCount()) {
				return new Transferable() {
					@Override
					public DataFlavor[] getTransferDataFlavors() {
						return new DataFlavor[]{dataFlavor};
					}

					@Override
					public boolean isDataFlavorSupported(DataFlavor flavor) {
						return flavor == dataFlavor;
					}

					@NotNull
					@Override
					public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
						return new TierViewDragData(selectedRow, getEditor().getSession().getTierView().get(selectedRow));
					}
				};
			} else {
				return null;
			}
		}
	}

}

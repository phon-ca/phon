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
package ca.phon.app.session.editor.view.tier_management;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.actions.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.Tuple;
import ca.phon.util.icons.*;
import com.jgoodies.forms.layout.*;
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
import java.util.stream.*;

/**
 * Panel for changing tier ordering, visibility and fonts.
 *
 */
public class TierOrderingEditorView extends EditorView {

	public final static String VIEW_NAME = "Tier Management";

	public final static String VIEW_ICON = IconManager.GoogleMaterialDesignIconsFontName + ":view_list";

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
		
		final NewTierAction addAction = new NewTierAction(getEditor(), this);
		addAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		addAction.putValue(FlatButton.ICON_NAME_PROP, "add");
		addAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		newTierButton = new FlatButton(addAction);
		newTierButton.setText(null);
		newTierButton.setFocusable(false);

		deleteAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		deleteAction.putValue(FlatButton.ICON_NAME_PROP, "remove");
		deleteAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		deleteTierButton = new FlatButton(deleteAction);
		deleteTierButton.setFocusable(false);
		
		final PhonUIAction<Void> upAction = PhonUIAction.runnable(this::moveUp);
		upAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		upAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_upward");
		upAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		upAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier up");
		moveUpButton = new FlatButton(upAction);
		moveUpButton.setFocusable(false);
		
		final PhonUIAction<Void> downAction = PhonUIAction.runnable(this::moveDown);
		downAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		downAction.putValue(FlatButton.ICON_NAME_PROP, "arrow_downward");
		downAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		downAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move tier down");
		moveDownButton = new FlatButton(downAction);
		moveDownButton.setFocusable(false);
		
		final PhonUIAction<Void> editAction = PhonUIAction.eventConsumer(this::onEditTier);
		editAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Edit tier...");
		editAction.putValue(FlatButton.ICON_FONT_NAME_PROP, IconManager.GoogleMaterialDesignIconsFontName);
		editAction.putValue(FlatButton.ICON_NAME_PROP, "edit");
		editAction.putValue(FlatButton.ICON_SIZE_PROP, IconSize.MEDIUM);
		editButton = new FlatButton(editAction);
		editButton.setFocusable(false);
		
//		final ToggleLockAllTiersAction lockAllAction = new ToggleLockAllTiersAction(getEditor(), this);
//		final JButton lockAllButton = new JButton(lockAllAction);
//
//		final ToggleHideAllTiersAction hideAllAction = new ToggleHideAllTiersAction(getEditor(), this);
//		final JButton hideAllButton = new JButton(hideAllAction);

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

		List<TierViewItem> hiddenTiers = tierView.stream().filter( tvi -> !tvi.isVisible() ).collect(Collectors.toList());

		TierViewItem tvi = tierView.get(tierViewIdx);
		// may be null
		SystemTierType systemTier = SystemTierType.tierFromString(tvi.getTierName());

		final ToggleTierVisibleAction toggleTierAct = new ToggleTierVisibleAction(getEditor(), this, tvi);
		builder.addItem(".", toggleTierAct);

		if(hiddenTiers.size() > 0) {
			JMenu showTierMenu = builder.addMenu(".", "Show hidden tier");
			for(TierViewItem hiddenTier:hiddenTiers) {
				final ToggleTierVisibleAction showTierAct = new ToggleTierVisibleAction(getEditor(), this, hiddenTier);
				showTierAct.putValue(Action.NAME, hiddenTier.getTierName());
				showTierMenu.add(showTierAct);
			}
		}

		final ToggleTierLockAction toggleTierLockAction = new ToggleTierLockAction(getEditor(), this, tvi);
		builder.addItem(".", toggleTierLockAction);

		builder.addSeparator(".", "font");

		if(!"default".equals(tvi.getTierFont())) {
			final ResetTierFontAction resetTierFontAction = new ResetTierFontAction(getEditor(), this, tvi);
			builder.addItem(".", resetTierFontAction);
		}

		JMenu fontMenu = builder.addMenu(".", "Font");
		setupFontMenu(new MenuBuilder(fontMenu), tvi);

		builder.addSeparator(".", "move");

		if(tierViewIdx > 0) {
			final MoveTierAction moveUpAction = new MoveTierAction(getEditor(), this, tvi, -1);
			builder.addItem(".", moveUpAction);
		}

		if(tierViewIdx < tierView.size()-1) {
			final MoveTierAction moveDownAction = new MoveTierAction(getEditor(), this, tvi, 1);
			builder.addItem(".", moveDownAction);
		}

		builder.addSeparator(".", "edit");
		final NewTierAction newTierAction = new NewTierAction(getEditor(), this, tierViewIdx+1);
		builder.addItem(".", newTierAction);

		Optional<TierDescription> tierDesc =
				StreamSupport.stream(getEditor().getSession().getUserTiers().spliterator(), false)
					.filter( td -> td.getName().equals(tvi.getTierName()) )
					.findAny();
		TierDescription td = (tierDesc.isPresent() ? tierDesc.get() :
				(systemTier != null ? SessionFactory.newFactory().createTierDescription(systemTier) :
					SessionFactory.newFactory().createTierDescription(tvi.getTierName()))
			);

		final DuplicateTierAction dupTierAction = new DuplicateTierAction(getEditor(), this, td, tierViewIdx+1);
		builder.addItem(".", dupTierAction);

		if(tierDesc.isPresent()) {
			final RemoveTierAction removeTierAction = new RemoveTierAction(getEditor(), this, tierDesc.get(), tvi);
			builder.addItem(".", removeTierAction);
		}
	}

	private void setupFontMenu(MenuBuilder builder, TierViewItem tvi) {
		final ImageIcon icon =
				IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL);
		final ImageIcon reloadIcon =
				IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
		final ImageIcon addIcon =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final ImageIcon subIcon =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final ImageIcon boldIcon =
				IconManager.getInstance().getIcon("actions/format-text-bold", IconSize.SMALL);
		final ImageIcon italicIcon =
				IconManager.getInstance().getIcon("actions/format-text-italic", IconSize.SMALL);

		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));

		final PhonUIAction<Tuple<TierViewItem, Integer>> toggleBoldAct =
				PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.BOLD));
		toggleBoldAct.putValue(PhonUIAction.NAME, "Bold");
		toggleBoldAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle bold modifier");
		toggleBoldAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isBold());
		toggleBoldAct.putValue(PhonUIAction.SMALL_ICON, boldIcon);
		builder.addItem(".", new JCheckBoxMenuItem(toggleBoldAct));

		final PhonUIAction<Tuple<TierViewItem, Integer>> toggleItalicAct =
				PhonUIAction.eventConsumer(this::onToggleStyle, new Tuple<>(tvi, Font.ITALIC));
		toggleItalicAct.putValue(PhonUIAction.NAME, "Italic");
		toggleItalicAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle italic modifier");
		toggleItalicAct.putValue(PhonUIAction.SELECTED_KEY, currentFont.isItalic());
		toggleItalicAct.putValue(PhonUIAction.SMALL_ICON, italicIcon);
		builder.addItem(".", new JCheckBoxMenuItem(toggleItalicAct));

		final PhonUIAction<TierViewItem> onIncreaseFontSize = PhonUIAction.eventConsumer(this::onIncreaseFontSize, tvi);
		onIncreaseFontSize.putValue(PhonUIAction.NAME, "Increase size");
		onIncreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase point size by 2");
		onIncreaseFontSize.putValue(PhonUIAction.SMALL_ICON, addIcon);
		builder.addItem(".", onIncreaseFontSize);

		final PhonUIAction<TierViewItem> onDecreaseFontSize = PhonUIAction.eventConsumer(this::onDecreaseFontSize, tvi);
		onDecreaseFontSize.putValue(PhonUIAction.NAME, "Decrease size");
		onDecreaseFontSize.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease point size by 2");
		onDecreaseFontSize.putValue(PhonUIAction.SMALL_ICON, subIcon);
		builder.addItem(".", onDecreaseFontSize);

		builder.addSeparator(".", "suggested-fonts");

		JMenuItem headerItem = new JMenuItem("-- Suggested Fonts --");
		headerItem.setEnabled(false);
		builder.addItem(".", headerItem);

		for(int i = 0; i < FontPreferences.SUGGESTED_IPA_FONT_NAMES.length; i++) {
			String suggestedFont = FontPreferences.SUGGESTED_IPA_FONT_NAMES[i];
			String fontString = String.format("%s-PLAIN-12", suggestedFont);
			// font not found
			if(Font.decode(fontString).getFamily().equals("Dialog")) continue;

			final PhonUIAction<Tuple<TierViewItem, Integer>> selectSuggestedFont =
					PhonUIAction.eventConsumer(this::onSelectSuggestedFont, new Tuple<>(tvi, i));
			selectSuggestedFont.putValue(PhonUIAction.NAME, suggestedFont);
			selectSuggestedFont.putValue(PhonUIAction.SHORT_DESCRIPTION, "Use font: " + suggestedFont);
			builder.addItem(".", selectSuggestedFont);
		}

		builder.addSeparator(".", "font-dialog");
		final PhonUIAction<TierViewItem> defaultAct = PhonUIAction.eventConsumer(this::onSelectFont, tvi);
		defaultAct.putValue(PhonUIAction.NAME, "Select font....");
		defaultAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select font using font selection dialog");
		defaultAct.putValue(PhonUIAction.LARGE_ICON_KEY, icon);
		builder.addItem(".", defaultAct);
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

	public void onSelectSuggestedFont(PhonActionEvent<Tuple<TierViewItem, Integer>> pae) {
		if(pae.getData() == null) return;
		Tuple<TierViewItem, Integer> tuple = pae.getData();
		TierViewItem tvi = tuple.getObj1();
		int idx = tuple.getObj2();

		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
		String suggestedFont = FontPreferences.SUGGESTED_IPA_FONT_NAMES[idx];
		float currentFontSize = currentFont.getSize();

		Font font = Font.decode(String.format("%s-PLAIN-%d", suggestedFont, (int)currentFontSize));

		TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), tvi.isVisible(),
				(new FontFormatter()).format(font), tvi.isTierLocked());

		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), tvi, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

	public void onIncreaseFontSize(PhonActionEvent<TierViewItem> pae) {
		TierViewItem tvi = pae.getData();
		if(tvi == null) return;

		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
		Font biggerFont = currentFont.deriveFont(Math.min(72.0f, currentFont.getSize() + 2.0f));

		TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), tvi.isVisible(),
				(new FontFormatter()).format(biggerFont), tvi.isTierLocked());

		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), tvi, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

	public void onDecreaseFontSize(PhonActionEvent<TierViewItem> pae) {
		TierViewItem tvi = pae.getData();
		if(tvi == null) return;

		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
		Font smallerFont = currentFont.deriveFont(Math.max(1.0f, currentFont.getSize() - 2.0f));

		TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), tvi.isVisible(),
				(new FontFormatter()).format(smallerFont), tvi.isTierLocked());

		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), tvi, newItem);
		getEditor().getUndoSupport().postEdit(edit);
	}

	public void onToggleStyle(PhonActionEvent<Tuple<TierViewItem, Integer>> pae) {
		if(pae.getData() == null) return;
		Tuple<TierViewItem, Integer> tuple = pae.getData();
		TierViewItem tvi = tuple.getObj1();
		int style = tuple.getObj2();

		Font currentFont = ("default".equals(tvi.getTierFont()) ? FontPreferences.getTierFont() : Font.decode(tvi.getTierFont()));
		int fontStyle = currentFont.getStyle();
		fontStyle ^= style;
		Font font = currentFont.deriveFont(fontStyle);

		TierViewItem newItem = SessionFactory.newFactory().createTierViewItem(tvi.getTierName(), tvi.isVisible(),
				(new FontFormatter()).format(font), tvi.isTierLocked());

		final TierViewItemEdit edit = new TierViewItemEdit(getEditor(), tvi, newItem);
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
	
	public void onEditTier(PhonActionEvent<Void> pae) {
		int selectedRow = tierOrderingTable.getSelectedRow();
		final List<TierViewItem> tierOrder = getCurrentOrder();
		
		if(selectedRow >= 0) {
			final TierViewItem tierItem = tierOrder.get(selectedRow);
			final EditTierAction act = new EditTierAction(getEditor(), this, tierItem);
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
						final RemoveTierAction act = new RemoveTierAction(getEditor(), this, td, tierItem);
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

	private final class TierTableTransferHandler extends TransferHandler {

		final static DataFlavor dataFlavor = new DataFlavor(TierViewItem.class, "Tier name");

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
				item = (TierViewItem) support.getTransferable().getTransferData(dataFlavor);
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
						return getEditor().getSession().getTierView().get(selectedRow);
					}
				};
			} else {
				return null;
			}
		}
	}

}

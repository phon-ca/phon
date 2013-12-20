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
package ca.phon.app.session.editor.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import ca.phon.app.prefs.PhonProperties;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.query.report.csv.CSVTableDataWriter;
import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.PromptedTextField.FieldState;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 */
public class SessionEditorQuickSearch {
	
	private static final Logger LOGGER = Logger
			.getLogger(SessionEditorQuickSearch.class.getName());
	
	/**
	 * Search field
	 */
	private SessionEditorQuickSearchField searchField;
	
	/**
	 * Session table
	 */
	private JTable table;
	
	/**
	 * Table model
	 */
	private FilterTableModel filterTableModel;
	private SessionScriptTableModel tableModel;
	
	/**
	 * Editor reference
	 */
	private WeakReference<SessionEditor> editorRef;
	
	/**
	 * Pop-up
	 */
	private Popup popup;
	
	/**
	 * Constructor
	 */
	public SessionEditorQuickSearch(SessionEditor editor) {
		super();
		editorRef = new WeakReference<SessionEditor>(editor);
		initComponents();
	}
	
	public JTable createTable() {
		final Font ipaFont = 
				PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
						Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
		
		final JXTable table = new JXTable();
		table.setColumnControlVisible(true);
		table.setSortable(true);
		table.setVisibleRowCount(20);
		table.getSelectionModel().addListSelectionListener(new SessionTableListener(table));
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addHighlighter(HighlighterFactory.createSimpleStriping(PhonGuiConstants.PHON_UI_STRIP_COLOR));
		table.setFont(ipaFont);
		
		return table;
	}
	
	private void initComponents() {
		final Font ipaFont = 
				PrefHelper.getFont(PhonProperties.IPA_TRANSCRIPT_FONT, 
						Font.decode(PhonProperties.DEFAULT_IPA_TRANSCRIPT_FONT));
		
		tableModel = 
				new SessionScriptTableModel(getEditor().getSession());
		filterTableModel = new FilterTableModel(tableModel);
		table = createTable();
		table.setModel(filterTableModel);
		table.setFocusable(false);
		
		searchField = new SessionEditorQuickSearchField(getEditor().getSession(), table);
		searchField.addPropertyChangeListener(SessionEditorQuickSearchField.INCLUDE_EXCLUDED_PROP, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				updateFilter();
			}
		});
		
		searchField.setColumnLabel("tier");
		searchField.setFont(ipaFont);
		searchField.setColumns(20);
		searchField.setAutoscrolls(true);
		searchField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					hideTablePopup();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}
		});
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(searchField.getState() == FieldState.INPUT) {
					updateFilter();
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				if(searchField.getState() == FieldState.INPUT) 
					updateFilter();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		searchField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				hideTablePopup();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				if(searchField.getText().length() > 0) {
					showTablePopup();
				}
			}
		});
		
		final ActionMap actionMap = searchField.getActionMap();
		final InputMap inputMap = searchField.getInputMap(JComponent.WHEN_FOCUSED);
		
		final PhonUIAction moveSelectionUp = 
				new PhonUIAction(this, "moveSelectionUp");
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		actionMap.put("_move_selection_up_", moveSelectionUp);
		inputMap.put(upKs, "_move_selection_up_");
		
		final PhonUIAction moveSelectionDown =
				new PhonUIAction(this, "moveSelectionDown");
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		actionMap.put("_move_selection_down_", moveSelectionDown);
		inputMap.put(downKs, "_move_selection_down_");
		
		final PhonUIAction openRecordListAct = 
				new PhonUIAction(this, "showRecordList");
		openRecordListAct.putValue(PhonUIAction.NAME, "Open Record List");
		openRecordListAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open record list with current filter.");
		searchField.setAction(openRecordListAct);
		
		searchField.setActionMap(actionMap);
		searchField.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
		
	}
	
	private void updateFilter() {
		//searchField.updateTableFilter();
		filterTableModel.setRowFilter(searchField.getRowFilter(searchField.getText()));
		if(searchField.getText().length() > 0) {
			showTablePopup();
		} else {
			hideTablePopup();
		}
	}
	
	public void showRecordList() {
		hideTablePopup();
		final SessionEditor editor = editorRef.get();
		
		final JTable table = createTable();
		table.setAutoCreateColumnsFromModel(true);
		if(searchField.getText().length() > 0) {
			final FilterTableModel tblModel = new FilterTableModel(filterTableModel);
			table.setModel(tblModel);
		} else {
			table.setModel(tableModel);
		}
		
		final String searchText = searchField.getText();
		table.setName("Record List : " + searchText);
		
		final PhonUIAction saveAct = new PhonUIAction(this, "saveAsCSV", table);
		saveAct.putValue(PhonUIAction.NAME, "Save as CSV...");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save table as CSV...");
		final ImageIcon saveIcon = IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcon);
		final JButton saveButton = new JButton(saveAct);
	
//		final PhonUIAction refreshAct = new PhonUIAction(this, "refreshRecordList", tblModel);
//		refreshAct.putValue(PhonUIAction.NAME, "Refresh table");
//		refreshAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Refresh table contents with filter.");
//		final ImageIcon refreshIcon = IconManager.getInstance().getIcon("actions/refresh", IconSize.SMALL);
//		refreshAct.putValue(PhonUIAction.SMALL_ICON, refreshIcon);
//		final JButton refreshButton = new JButton(refreshAct);
		
		final JPanel resultListPanel = new JPanel(new BorderLayout());
		final JComponent buttonPanel = ButtonBarFactory.buildRightAlignedBar(saveButton);
		resultListPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		final JScrollPane tableScroller = new JScrollPane(table);
		resultListPanel.add(tableScroller, BorderLayout.CENTER);
		
		final Point popupPoint = new Point(
				0, searchField.getHeight());
		SwingUtilities.convertPointToScreen(popupPoint, searchField);
		final Dimension prefSize = resultListPanel.getPreferredSize();
		
		int rightx = popupPoint.x + prefSize.width;
		if(rightx > Toolkit.getDefaultToolkit().getScreenSize().width) {
			int diff = rightx - Toolkit.getDefaultToolkit().getScreenSize().width;
			popupPoint.x -= diff;
		}
		
		editor.getViewModel().showDynamicFloatingDockable("Record List : " + searchText, resultListPanel, 
				popupPoint.x, popupPoint.y, prefSize.width, prefSize.height);
		table.requestFocusInWindow();
	}
	
	public void refreshRecordList(FilterTableModel model) {
		model.setRowFilter(model.getRowFilter());
	}
	
	public void saveAsCSV(JTable table) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(getEditor());
		props.setFileFilter(FileFilter.csvFilter);
		props.setCanCreateDirectories(true);
		props.setInitialFile("record_list.csv");
		props.setTitle("Save as CSV");
		props.setRunAsync(false);
		
		final String saveAs = NativeDialogs.showSaveDialog(props);
		if(saveAs != null) {
			// create a new CSVTableWriter
			final CSVTableDataWriter writer = new CSVTableDataWriter();
			try {
				writer.writeTableToFile(table, new File(saveAs));
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				
				final Toast toast = ToastFactory.makeToast("Unable to save table: " + e.getLocalizedMessage());
				toast.start(table);
			}
		}
	}
	
	public void moveSelectionUp() {
		if(table != null) {
			final int currentSelection = table.getSelectedRow();
			if(currentSelection > 0) {
				table.getSelectionModel().setSelectionInterval(currentSelection-1, currentSelection-1);
			}
		}
	}
	
	public void moveSelectionDown() {
		if(table != null) {
			final int currentSelection = table.getSelectedRow();
			if(currentSelection < table.getRowCount()-1) {
				table.getSelectionModel().setSelectionInterval(currentSelection+1, currentSelection+1);
			}
		}
	}
	
	public void setupEditorActions() {
		final DelegateEditorAction tierChangeAct = new DelegateEditorAction(this, "onTierDataChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_CHANGE_EVT, tierChangeAct);
		
		final DelegateEditorAction tierNumberChangedAct = new DelegateEditorAction(this, "onTierNumberChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.TIER_VIEW_CHANGED_EVT, tierNumberChangedAct);
		
		final DelegateEditorAction recordNumberChangedAct = new DelegateEditorAction(this, "onRecordNumberChanged");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_DELETED_EVT, recordNumberChangedAct);
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_ADDED_EVT, recordNumberChangedAct);
		
		getEditor().getEventManager().registerActionForEvent(EditorEventType.RECORD_EXCLUDE_CHANGE_EVT, tierChangeAct);
		
		final DelegateEditorAction closingAct = new DelegateEditorAction(this, "onEditorClosing");
		getEditor().getEventManager().registerActionForEvent(EditorEventType.EDITOR_CLOSING, closingAct);
	}
	
	@RunOnEDT
	public void onEditorClosing(EditorEvent ee) {
		// shut down exeService
		filterTableModel.cleanup();
	}
	
	@RunOnEDT
	public void onTierDataChanged(EditorEvent ee) {
		// get the current record index
		final int recIdx = getEditor().getCurrentRecordIndex();
		tableModel.setRowDirty(recIdx);
	}
	
	@RunOnEDT
	public void onTierNumberChanged(EditorEvent ee) {
		// reset column layout
		tableModel.resetColumns();
	}
	
	@RunOnEDT
	public void onRecordNumberChanged(EditorEvent ee) {
		tableModel.clearCache();
		tableModel.fireTableDataChanged();
	}
	
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	public JTextField getSearchField() {
		return this.searchField;
	}
	
	public JTable getTable() {
		return this.table;
	}
	
	public Popup getPopup() {
		if(popup == null) {
			final JScrollPane tableScroller = new JScrollPane(table);
			tableScroller.setAutoscrolls(true);
			
			final Point popupPoint = new Point(
					0, searchField.getHeight());
			SwingUtilities.convertPointToScreen(popupPoint, searchField);
			final Dimension prefSize = tableScroller.getPreferredSize();
			
			int rightx = popupPoint.x + prefSize.width;
			if(rightx > Toolkit.getDefaultToolkit().getScreenSize().width) {
				int diff = rightx - Toolkit.getDefaultToolkit().getScreenSize().width;
				popupPoint.x -= diff;
			}
			popup = PopupFactory.getSharedInstance().getPopup(searchField, tableScroller, popupPoint.x, popupPoint.y);
		}
		return this.popup;
	}
	
	public void showTablePopup() {
		getPopup().show();
	}
	
	public void hideTablePopup() {
		getPopup().hide();
		popup = null;
	}
	
	/**
	 * Table selection listener
	 *
	 */
	private class SessionTableListener implements ListSelectionListener {
		
		private JTable table;
		
		public SessionTableListener(JTable table) {
			this.table = table;
		}

		private boolean isPopup() {
			return this.table == SessionEditorQuickSearch.this.table;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()) {
				// don't change record if we are not the focused table
				if(!isPopup() && !table.hasFocus()) return;
				int selectedRow = table.getSelectedRow();
				int uttIdx = table.convertRowIndexToModel(selectedRow);
				
				// get correct uttIdx from filter
				if(table.getModel() instanceof FilterTableModel) {
					final FilterTableModel tblModel = (FilterTableModel)table.getModel();
					uttIdx = tblModel.modelToDelegate(uttIdx);
				}
				
				if(uttIdx >= 0) {
					getEditor().setCurrentRecordIndex(uttIdx);
					table.scrollRectToVisible(table.getCellRect(selectedRow, 0, true));
				}
			}
		}
		
	}
}

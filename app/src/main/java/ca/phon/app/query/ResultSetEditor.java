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
package ca.phon.app.query;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.DefaultHighlighter.*;
import javax.swing.text.Highlighter.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;

import com.jgoodies.forms.layout.*;

import ca.phon.app.log.*;
import ca.phon.app.project.*;
import ca.phon.app.query.report.*;
import ca.phon.app.session.editor.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.report.*;
import ca.phon.query.report.csv.*;
import ca.phon.query.report.io.*;
import ca.phon.query.report.util.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.layout.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.text.*;
import ca.phon.ui.toast.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

/**
 * An viewer/editor window for result sets.  For
 * displaying a table of results, use ResultSetTable.
 *
 */
public class ResultSetEditor extends ProjectFrame {
	
	private static final long serialVersionUID = -4309831950609525140L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ResultSetEditor.class.getName());
	
	/* 
	 * Actions buttons
	 */
	private Action saveAction;
	private Action saveTableAction;
	private Action inventoryAction;
	private Action editColumnsAction;
	private Action toggleExcludedAction;
	private Action toggleShowExcludedAction;
	
	private JToolBar toolbar;
	private JButton saveButton;
	private JButton saveTableButton;
	private JButton inventoryButton;
	private JButton editColumnsButton;
	
	private JXStatusBar statusBar;
	private JLabel statusLabel;
	
	/*
	 * result table
	 */
	private JCheckBox showExcludedBox;
	private TableSearchField tableSearchField;
	private JXTable resultTable;
	
	/*
	 * Query info
	 */
	private Query query;
	private ResultSet resultSet;
	private Session session;
	
	/*
	 * Temp project.  When set, the results are saved to this
	 * project location.
	 */
	private Project tempProject;
	
	private EditorAction recordChangedAct;
	
	/**
	 * Constructor
	 * @param project
	 * @param query
	 * @param session
	 * @param rs
	 */
	public ResultSetEditor(Project project, Query query, ResultSet rs) {
		super();
		super.setProject(project);
		this.query = query;
		this.resultSet = rs;
		
		try {
			this.session = project.openSession(rs.getCorpus(), rs.getSession());
			putExtension(Session.class, this.session);
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		// setup title
		super.setWindowName(query.getName() + "/" + rs.getSessionPath());
		
		init();
	}
	
	/**
	 * Try to attach to a parent frame.  Does nothing
	 * if already attached to an editor.
	 */
	public boolean attachToEditor() {
		if(getParentFrame() == null) {
			// look for parent frames
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(cmf instanceof SessionEditor) {
					final Session transcript = ((SessionEditor)cmf).getSession();
					if(transcript.getCorpus().equals(resultSet.getCorpus())
							&& transcript.getName().equals(resultSet.getSession())) {
						super.setParentFrame(cmf);
						final SessionEditor editor = getEditor();
						
						recordChangedAct = 
								new DelegateEditorAction(this, "onRecordChange");
						editor.getEventManager().registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);
						
						break;
					}
				}
			}
		}
		return getParentFrame() != null;
	}
	
	/*
	 * Editor event for record changes
	 */
	public void onRecordChange(EditorEvent ee) {
		resultTable.repaint();
	}
	
	public SessionEditor getEditor() {
		return (attachToEditor() ? (SessionEditor)getParentFrame() : null);
	}
	
	private void init() {
		setupToolbar();
		setupResultTable();
		
		final JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BorderLayout());
		
		final FormLayout topLayout = new FormLayout(
				"left:pref, right:pref:grow", "pref");
		final CellConstraints cc = new CellConstraints();
		final JPanel topPanel = new JPanel(topLayout);
		topPanel.add(showExcludedBox, cc.xy(1, 1));
		topPanel.add(tableSearchField, cc.xy(2, 1));

		final JScrollPane tblScroller = new JScrollPane(resultTable, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		resultsPanel.add(topPanel, BorderLayout.NORTH);
		resultsPanel.add(tblScroller, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		add(resultsPanel, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		
		updateStatus();
	}
	
	public ResultListing getListing() {
		final ResultSetListingManager manager = new ResultSetListingManager();
		ResultListing retVal = null;
		try {
			final ResultListing listing = manager.getResultListing(getProject(), query, resultSet);
			retVal = listing;
		} catch (IOException e) {
			// don't do anything, just create a new
			// listing 
		}
		if(retVal == null) {
			// create a new, default result listing
			final ObjectFactory factory = new ObjectFactory();
			retVal = factory.createResultListing();
			for(ResultListingField field:ResultListingFieldBuilder.getDefaultFields(resultSet)) {
				retVal.getField().add(field);
			}
		}
		return retVal;
	}
	
	private void setupToolbar() {
		setupButtons();
		
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		toolbar.add(saveButton);
		toolbar.add(saveTableButton);
		toolbar.addSeparator();
//		toolbar.add(inventoryButton);
		toolbar.add(editColumnsButton);
	}
	
	private void setupButtons() {
		// save button
		saveButton = new JButton(getSaveAction());
		saveButton.setEnabled(false);
		saveTableButton = new JButton(getSaveTableAction());
//		inventoryButton = new JButton(getInventoryAction());
		editColumnsButton = new JButton(getEditColumnsAction());
	}
	
	/* Save action */
	private Action getSaveAction() {
		if(this.saveAction == null) {
			final ImageIcon icon =
					IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
			
			saveAction = new PhonUIAction(this, "saveData");
			saveAction.putValue(PhonUIAction.NAME, "Save");
			final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
			saveAction.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
			saveAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save result set");
			saveAction.putValue(PhonUIAction.SMALL_ICON, icon);
		}
		return this.saveAction;
	}
	
	/*
	 * Save table as csv action
	 */
	private Action getSaveTableAction() {
		if(this.saveTableAction == null) {
			final ImageIcon icon =
					IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
			
			saveTableAction = new PhonUIAction(this, "saveTable");
			saveTableAction.putValue(PhonUIAction.NAME, "Save table as CSV...");
			saveTableAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save result table as CSV.");
			final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_S,
					KeyEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
			saveTableAction.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
			saveTableAction.putValue(PhonUIAction.SMALL_ICON, icon);
		}
		return saveTableAction;
	}
	
	public void saveTable() {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(this);
		props.setRunAsync(false);
		props.setTitle("Save as CSV");
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.csvFilter);
		final String saveAs = NativeDialogs.showSaveDialog(props);
		if(saveAs != null) {
			// create a new CSVTableWriter
			final CSVTableDataWriter writer = new CSVTableDataWriter();
			try {
				writer.writeTableToFile(resultTable, new File(saveAs));
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				
				final Toast toast = ToastFactory.makeToast("Unable to save table: " + e.getLocalizedMessage());
				toast.start(saveTableButton);
			}
		}
	}
	
	public JTable getTable() {
		return this.resultTable;
	}
	
	/*
	 * Inventory action
	 */
	private Action getInventoryAction() {
		if(this.inventoryAction == null) {
			final ImageIcon icon =
					IconManager.getInstance().getIcon("actions/inventory", IconSize.SMALL);
			
			inventoryAction = new PhonUIAction(this, "generateInventory");
			inventoryAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Generate inventory...");
			inventoryAction.putValue(PhonUIAction.SMALL_ICON, icon);
		}
		return inventoryAction;
	}
	
	public void generateInventory() {
		// setup the inventory dialog
		final ObjectFactory factory = new ObjectFactory();
		final InventorySection invSection = factory.createInventorySection();
		final InventorySectionPanel panel = new InventorySectionPanel(invSection);
		final JPanel invPanel = panel.getOptionsPanel();
		
		final JDialog inventoryDialog = new JDialog(this, true);
		
	}
	
	/*
	 * Edit columns
	 */
	private Action getEditColumnsAction() {
		if(this.editColumnsAction == null) {
			final ImageIcon icon =
					IconManager.getInstance().getIcon("actions/edit", IconSize.SMALL);
			
			editColumnsAction = new PhonUIAction(this, "editColumns");
			editColumnsAction.putValue(PhonUIAction.NAME, "Edit table columns...");
			editColumnsAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Set up result table columns.");
			editColumnsAction.putValue(PhonUIAction.SMALL_ICON, icon);
		}
		return this.editColumnsAction;
	}
	
	public void editColumns() {
		final DialogHeader header = new DialogHeader("Edit Columns", "Set up result table columns.");
		final JPanel panel = new JPanel(new BorderLayout());
		
		final ResultListing listing = getListing();
		final ResultListingSectionPanel p = new ResultListingSectionPanel(listing);
		final JPanel resultListingEditor = p.getFieldPanel();
		
		panel.add(header, BorderLayout.NORTH);
		panel.add(resultListingEditor, BorderLayout.CENTER);
		
		final JDialog editColumnsDialog = new JDialog(this, true);
		editColumnsDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				editColumnsDialog.dispose();
				
				final ResultListingTableModel tblModel =
						(ResultListingTableModel)resultTable.getModel();
				tblModel.setListing(listing);
				
				// save listing
				final ResultSetListingManager manager = new ResultSetListingManager();
				try {
					manager.saveResultListing(getProject(), query, resultSet, listing);
				} catch (IOException e) {
					// not critical, but should report the 'why'
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
		});
		
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editColumnsDialog.dispose();
			}
		});
		
		final JComponent buttonBar = ButtonBarBuilder.buildOkCancelBar(okButton, cancelButton);
		
		panel.add(buttonBar, BorderLayout.SOUTH);
		
		editColumnsDialog.add(panel);
		editColumnsDialog.pack();
		editColumnsDialog.setSize(new Dimension(500, 400));
		editColumnsDialog.setLocationRelativeTo(this);
		editColumnsDialog.setVisible(true);
	}
	
	/*
	 * toggle current result exclusion
	 */
	private Action getToggleExcludedAction() {
		if(this.toggleExcludedAction == null) {
			toggleExcludedAction = new PhonUIAction(this, "toggleExcluded");
			toggleExcludedAction.putValue(PhonUIAction.NAME, "Toggle result excluded");
			toggleExcludedAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle current result exclusion");
			final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
			toggleExcludedAction.putValue(PhonUIAction.ACCELERATOR_KEY, ks);
		}
		return toggleExcludedAction;
	}
	
	public void toggleExcluded() {
		final int selectedRow = resultTable.getSelectedRow();
		final ResultListingTableModel tblModel = (ResultListingTableModel)resultTable.getModel();
		for(int rowIdx:resultTable.getSelectedRows()) {
			final int resultIdx = resultTable.convertRowIndexToModel(rowIdx);
			if(resultIdx >= 0) {
				final Result r = resultSet.getResult(resultIdx);
				r.setExcluded(!r.isExcluded());
				
				setModified(true);
				tblModel.fireTableRowsUpdated(resultIdx, resultIdx);
			}
		}
		
	
		if(selectedRow >= 0 && selectedRow < resultTable.getRowCount()) {
			resultTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
		}
	}
	
	/*
	 * Toggle show excluded results
	 */
	private Action getToggleShowExcludedAction() {
		if(this.toggleShowExcludedAction == null) {
			toggleShowExcludedAction = new PhonUIAction(this, "toggleShowExcluded");
			toggleShowExcludedAction.putValue(PhonUIAction.NAME, "Show excluded results");
		}
		return this.toggleShowExcludedAction;
	}
	
	public void toggleShowExcluded() {
		updateRowFilter();
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		// add a 'Results' menu
		final JMenu resultsMenu = new JMenu("Results");
	
		resultsMenu.add(getToggleExcludedAction());
		resultsMenu.addSeparator();
//		resultsMenu.add(getInventoryAction());
		resultsMenu.add(getEditColumnsAction());
		
		menuBar.add(resultsMenu, 2);
		
		// get the file menu
		JMenu fileMenu = null;
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			if(menuBar.getMenu(i).getText().equals("File")) {
				fileMenu = menuBar.getMenu(i);
				break;
			}
		}
		
		if(fileMenu != null) {
			fileMenu.add(new JMenuItem(getSaveTableAction()), 0);
			fileMenu.add(new JMenuItem(getSaveAction()), 0);
		}
	}
	
	private void updateStatus() {
		final int row = resultTable.getSelectedRow();
		final String rowText = (row < 0 ? "?" : (row+1) + "");
		final String numRows = resultTable.getRowCount() + "";
		
		String retVal = rowText + "/" + numRows;
		
		final int numResults = resultSet.numberOfResults(true);
		if(numResults != resultTable.getRowCount()) {
			final String numFiltered = (numResults - resultTable.getRowCount()) + "";
			retVal += " (" + numFiltered + " hidden)";
		}
			
		statusLabel.setText(retVal);
	}

	private void setupResultTable() {
		final ResultListingTableModel model = new ResultListingTableModel(session, resultSet, getListing());
		resultTable = new JXTable(model);
		resultTable.setColumnControlVisible(true);
		resultTable.setFont(FontPreferences.getTierFont());
		resultTable.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				resultTable.requestFocusInWindow();
				resultTable.removeMouseListener(this);
			}
		});
		
		final Highlighter stripeHighlighter = HighlighterFactory.createSimpleStriping();
		resultTable.addHighlighter(stripeHighlighter);
		resultTable.addHighlighter(currentRecordHighlighter);
		resultTable.addHighlighter(excludedHighlighter);
		
		final String toggleId = "_toggle_result_excluded_";
		final Action toggleAct = getToggleExcludedAction();
		final InputMap inputMap = resultTable.getInputMap(JComponent.WHEN_FOCUSED);
		final ActionMap actionMap = resultTable.getActionMap();
		actionMap.put(toggleId, toggleAct);
		final KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		inputMap.put(ks, toggleId);
		
		resultTable.setActionMap(actionMap);
		resultTable.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.getSelectionModel().addListSelectionListener(tableSelectionListener);
		
		// search field
		tableSearchField = new TableSearchField(resultTable, false) {

			@Override
			public void setTableFilter(String expr) {
				updateRowFilter();
			}
			
		};
		tableSearchField.setColumns(20);
		tableSearchField.setPrompt("Filter results");
		tableSearchField.getTextField().setFont(FontPreferences.getTierFont());
		tableSearchField.getTextField().addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				updateRowFilter();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		// toggle excluded results
		showExcludedBox = new JCheckBox(getToggleShowExcludedAction());
		
		statusLabel = new JLabel();
		statusBar = new JXStatusBar();
		statusBar.add(statusLabel);
		
		updateRowFilter();
	}
	
	public void setModified(boolean modified) {
		super.setModified(modified);
		saveButton.setEnabled(modified);
	}

	public Project getTempProject() {
		return this.tempProject;
	}
	
	public void setTempProject(Project project) {
		this.tempProject = project;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public Query getQuery() {
		return this.query;
	}
	
	public ResultSet getResultSet() {
		return this.resultSet;
	}
	
	@Override
	public boolean saveData() throws IOException {
		final QueryManager qm = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = qm.createResultSetManager();
		rsManager.saveResultSet((getTempProject() != null ? getTempProject() : getProject()), query, resultSet);
		setModified(false);
		
		return true;
	}
	
	
	
	@Override
	public void close() {
		if(getEditor() != null) {
			if(getEditor().getSelectionModel() != null) 
				getEditor().getSelectionModel().clear();
			if(getEditor().getEventManager() != null)
				getEditor().getEventManager().removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, recordChangedAct);
		}
		super.close();
	}

	private void updateRowFilter() {
		// get the row filter (if any) from the search field
		final RowFilter<TableModel, Integer> searchFieldFilter =
				tableSearchField.getRowFilter(tableSearchField.getText());
		final List<RowFilter<TableModel, Integer>> filters =
				new ArrayList<RowFilter<TableModel,Integer>>();
		filters.add(excludedFilter);
		if(searchFieldFilter != null) {
			filters.add(searchFieldFilter);
		}
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(resultTable.getModel());
		sorter.setSortsOnUpdates(true);
		final RowFilter<TableModel, Integer> filter = RowFilter.andFilter(filters);
		sorter.setRowFilter(filter);
		resultTable.setRowSorter(sorter);
		
		updateStatus();
	}
	
	/*
	 * Table highlighter for excluded results (when shown) 
	 */
	private final HighlightPredicate excludedPredicate = new HighlightPredicate() {

		@Override
		public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
			if(showExcludedBox.isSelected()) {
				final int rowIdx = arg1.row;
				final int resultIdx = resultTable.convertRowIndexToModel(rowIdx);
				final Result result = resultSet.getResult(resultIdx);
				return result.isExcluded();
			} else {
				return false;
			}
		}
		
	};
	
	private final Highlighter excludedHighlighter = 
			new ColorHighlighter(excludedPredicate, new Color(0, 0, 0, 0), Color.gray) {

				@Override
				protected void applyForeground(Component renderer,
						ComponentAdapter adapter) {
					super.applyForeground(renderer, adapter);
					
					if(renderer instanceof JLabel) {
						JLabel lbl = (JLabel)renderer;
						lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
					}
				}

				@Override
				public void applyBackground(Component renderer, ComponentAdapter adapter) {
				}
				
	};
	
	/*
	 * Row highlighter for current record results
	 *  
	 */
	private final HighlightPredicate currentRecordPredicate = new HighlightPredicate() {

		@Override
		public boolean isHighlighted(Component arg0, ComponentAdapter arg1) {
			boolean retVal = false;
			
			// TODO 
//			if(getEditor() != null) {
//				final int rowIdx = arg1.row;
//				final int resultIdx = resultTable.convertRowIndexToModel(rowIdx);
//				final Result r = resultSet.getResult(resultIdx);
//				
//				final int currentRecord = getEditor().getModel().getCurrentIndex();
//				retVal = (currentRecord == r.getRecordIndex());
//			}
			
			return retVal;
		}
		
	};
	
	private final Highlighter currentRecordHighlighter = 
			new ColorHighlighter(currentRecordPredicate, Color.yellow, Color.black);
	
	/*
	 * Table selection listener
	 */
	private final ListSelectionListener tableSelectionListener = new ListSelectionListener() {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting() && getEditor() != null) {
				final int rowIdx = resultTable.getSelectedRow();
				if(rowIdx < 0) return;
				
				final int resultIdx = resultTable.convertRowIndexToModel(rowIdx);
				if(resultIdx >= 0) {
					final Result r = resultSet.getResult(resultIdx);
					final EditorSelectionModel selectionModel = getEditor().getSelectionModel();
					selectionModel.clear();
					
					int numPrimaryRvs = 1;
					switch(r.getSchema()) {
					case "LINEAR":
						numPrimaryRvs = 1;
						break;
						
					case "ALIGNED":
						numPrimaryRvs = 2;
						break;
						
					case "DETECTOR":
						numPrimaryRvs = 4;
						break;
						
					default:
						break;
					}
					
					for(int rvIdx = 0; rvIdx < r.getNumberOfResultValues(); rvIdx++) {
						ResultValue rv = r.getResultValue(rvIdx);
						final Range range = new Range(rv.getRange().getFirst(), rv.getRange().getLast(), false);
						final SessionEditorSelection selection = 
								new SessionEditorSelection(r.getRecordIndex(), rv.getTierName(),
										rv.getGroupIndex(), range);
						
						if(rvIdx >= numPrimaryRvs) {
							// draw secondary highlights with a box instead of 
							// default highlight painter
							selection.putExtension(HighlightPainter.class, 
									new OutlineHighlightPainter(PhonGuiConstants.PHON_SELECTED));
						}
						
						selectionModel.addSelection(selection);
					}
					if(getEditor().getCurrentRecordIndex() != r.getRecordIndex()) 
						getEditor().setCurrentRecordIndex(r.getRecordIndex());
				} else {
					getEditor().getSelectionModel().clear();
				}
				
				// update status label
				updateStatus(); 
			}
		}
	};
	
	private class OutlineHighlightPainter implements HighlightPainter {
		
		private final Color color;
		
		public OutlineHighlightPainter(Color color) {
			this.color = color;
		}

		@Override
		public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
			g.setColor(color);
			Rectangle r = bounds.getBounds();
			try {
				Rectangle2D p0r = c.modelToView2D(p0);
				Rectangle2D p1r = c.modelToView2D(p1);
				g.drawRect((int)p0r.getX(), r.y, (int)(p1r.getX()-p0r.getX()), r.height-1);
			} catch (BadLocationException e) {
				LogUtil.warning(e);
			}
		}
		
	}
	
	/**
	 * Excluded table filter
	 */
	private final RowFilter<TableModel, Integer> excludedFilter = 
			new RowFilter<TableModel, Integer>() {

			@Override
			public boolean include(
					javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
				final int resultIdx = entry.getIdentifier();
				final Result result = resultSet.getResult(resultIdx);
				
				if(showExcludedBox.isSelected() && result.isExcluded()) 
					return true;
				else {
					return !result.isExcluded();
				}
			}
		
	};
	
}

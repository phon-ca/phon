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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.joda.time.DateTime;

import ca.phon.app.query.EditQueryDialog.ReturnStatus;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.DateFormatter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.StarBox;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.text.TableSearchField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Shows the query history for a given project.
 *
 */
public class QueryHistory extends CommonModuleFrame {
	
	private final static Logger LOGGER = Logger
			.getLogger(QueryHistory.class.getName());

	private static final long serialVersionUID = 6189265050306021216L;

	/** The header */
	private DialogHeader header;
	
	private QueryHistoryTableModel queryModel;
	private JXTable queryTable;
	private TableRowSorter<QueryHistoryTableModel> queryRowSorter;
	
	/**
	 * Table filter
	 */
	private TableSearchField tblSearchField;
	private StarBox onlyStarredBox;
	private JButton refreshButton;
	
	/**
	 * Query info panel
	 */
	private QueryInfoPanel infoPanel;
	
	/**
	 * Constructor
	 */
	public QueryHistory(Project project) {
		super();
		
		String titleString = "Phon : ";
		titleString += project.getName() + " : Query History";
		
		super.setTitle(titleString);
		super.setWindowName("Query History");
		
		super.putExtension(Project.class, project);
		
		init();
	}
	
	private void updateWindow(Query q) {
		infoPanel.setQuery(q);
	}
	
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		final Project project = getProject();
		header = new DialogHeader("Query History", project.getName());
		
		queryModel = new QueryHistoryTableModel(project);
		queryTable = new JXTable(queryModel);
		queryRowSorter = new TableRowSorter<QueryHistoryTableModel>(queryModel);
		final RowSorter.SortKey sortKey = new RowSorter.SortKey(QueryHistoryTableModel.Columns.Date.ordinal(), SortOrder.ASCENDING);
		queryRowSorter.setSortKeys(Collections.singletonList(sortKey));
		queryRowSorter.setSortsOnUpdates(true);
		queryTable.setRowSorter(queryRowSorter);
		queryTable.addHighlighter(HighlighterFactory.createSimpleStriping());
		queryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryTable.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE
						|| e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					int selectedRow = queryTable.getSelectedRow();
					if(selectedRow < 0) return;
					selectedRow = queryTable.convertRowIndexToModel(selectedRow);
					Query q = queryModel.getQueryForRow(selectedRow);
					if(q != null) {
						deleteQuery(q);
					}
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
		});
		queryTable.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.isPopupTrigger()) {
					showQueryContextMenu(e.getPoint());
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				showMenu(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				showMenu(e);
			}
			
			private void showMenu(MouseEvent e) {
				if(e.isPopupTrigger()) {
					int selectedRow = queryTable.rowAtPoint(e.getPoint());
					if(selectedRow < 0) return;					
					queryTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
					selectedRow = queryTable.convertRowIndexToModel(selectedRow);
//					if(selectedRow >= 0 && selectedRow < queryTable.getModel().r)
					showQueryContextMenu(e.getPoint());
				}
				super.mousePressed(e);
			}
			
		});
		
		queryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				int selectedRow = queryTable.getSelectedRow();
				if(selectedRow < 0) return;
				selectedRow = queryTable.convertRowIndexToModel(selectedRow);
				final Query q = queryModel.getQueryForRow(selectedRow);
				if(q != null) {
					final Runnable update = new Runnable() { public void run() {
						updateWindow(q);
					}};
					SwingUtilities.invokeLater(update);
				}
			}
			
		});
		
		queryTable.getColumn(QueryHistoryTableModel.Columns.Date.ordinal()).setCellRenderer(
				new DateRenderer());
//		queryTable.getColumn(QueryHistoryTableModel.Columns.Starred.ordinal()).setCellRenderer(
//				new StarRenderer());
		queryTable.setColumnControlVisible(true);
		queryModel.update();
		
		tblSearchField = new TableSearchField(queryTable, false);
		tblSearchField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				tblSearchField.updateTableFilter();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		infoPanel = new QueryInfoPanel(project);
		
		ImageIcon refreshIcon = IconManager.getInstance().getIcon("actions/reload", IconSize.SMALL);
		PhonUIAction refreshAct = new PhonUIAction(queryModel, "update");
		refreshAct.putValue(PhonUIAction.SMALL_ICON, refreshIcon);
		refreshAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Refresh query table");
		refreshButton = new JButton(refreshAct);
		
		onlyStarredBox = new StarBox(IconSize.SMALL);
		onlyStarredBox.setToolTipText("Only show starred queries");
		onlyStarredBox.setSelected(false);
		onlyStarredBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				queryModel.setStarredOnly(onlyStarredBox.isSelected());
				queryModel.update();
			}
		});
		
		FormLayout filterLayout = new FormLayout(
				"pref, fill:pref:grow, pref",
				"pref");
		CellConstraints cc = new CellConstraints();
		JPanel filterPanel = new JPanel(filterLayout);
		
		filterPanel.add(tblSearchField, cc.xy(2, 1));
		filterPanel.add(onlyStarredBox, cc.xy(1, 1));
		filterPanel.add(refreshButton, cc.xy(3, 1));
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(filterPanel, BorderLayout.NORTH);
		leftPanel.add(new JScrollPane(queryTable), BorderLayout.CENTER);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(infoPanel);
		splitPane.setOneTouchExpandable(true);
		
		add(header, BorderLayout.NORTH);
		add(splitPane,  BorderLayout.CENTER);
	}
	
	/*
	 * Display the query table context menu
	 * 
	 */
	private void showQueryContextMenu(Point p) {
		JPopupMenu menu = new JPopupMenu();
		
		final int selectedRow = queryTable.getSelectedRow();
		if(selectedRow < 0) return;
		final int modelRow = queryTable.convertRowIndexToModel(selectedRow);
		final Query q = queryModel.getQueryForRow(modelRow);
		if(q != null) {
			
			JMenuItem loadQueryItem = new JMenuItem("Open query...");
			loadQueryItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					openInEditor(q);
				}
				
			});
			menu.add(loadQueryItem);
			
			JMenuItem editQueryItem = new JMenuItem("Edit details...");
			editQueryItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					editQuery(q);
					queryModel.fireTableRowsUpdated(selectedRow, selectedRow);
				}
			});
			menu.add(editQueryItem);
			
			String starItemText = 
				(q.isStarred() ? "Set unstarred" : "Set starred" );
			JMenuItem starItem = new JMenuItem(starItemText);
			starItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toggleStarred(q);
					infoPanel.updateForm();
//					queryModel.fireTableCellUpdated(selectedRow, QueryHistoryTableModel.Columns.Starred.ordinal());
				}
				
			});
			menu.add(starItem);
			menu.addSeparator();
			
			JMenuItem deleteItem = new JMenuItem("Delete");
			KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
			deleteItem.setAccelerator(ks);
			deleteItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					deleteQuery(q);
				}
				
			});
			menu.add(deleteItem);
			
			
			
			// display menu
			menu.show(queryTable, p.x, p.y);
		}
	}
	
	private void deleteQuery(Query q) {
		String deleteMsg = 
			"Delete query '" + q.getName() + "'?  Action cannot be undone.";
		int retVal = 
			NativeDialogs.showOkCancelDialogBlocking(QueryHistory.this, 
					"", "Delete query", deleteMsg);
		
		if(retVal == 0) {
			final QueryManager qManager = QueryManager.getSharedInstance();
			final ResultSetManager rsManager = qManager.createResultSetManager();
			try {
				rsManager.deleteQuery(getProject(), q);
				queryModel.removeQuery(q);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	private void editQuery(Query q) {
		final EditQueryDialog editQueryDialog = new EditQueryDialog(getProject(), q);
		editQueryDialog.pack();
		editQueryDialog.setLocationRelativeTo(queryTable);
		if(editQueryDialog.showModal() == ReturnStatus.OK
				&& editQueryDialog.hasChanged()) {
			final QueryManager qManager = QueryManager.getSharedInstance();
			final ResultSetManager rsManager = qManager.createResultSetManager();
			
			try {
				rsManager.saveQuery(getProject(), q);
				
				// update info panel
				infoPanel.updateForm();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}
	
	private void openInEditor(Query q) {
		QueryScript script = new QueryScript(q.getScript().getSource());
		ScriptParameters params = new ScriptParameters();
		try {
			params = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		for(ScriptParam sp:params) {
			for(String id:sp.getParamIds()) {
				Object v = q.getScript().getParameters().get(id);
				if(v != null) {
					sp.setValue(id, v);
				}
			}
		}
		
		QueryEditorWindow sd = 
			new QueryEditorWindow("Script Editor", getProject(),
					script);
		sd.pack();
		sd.setLocationByPlatform(true);
		sd.setVisible(true);
	}
	
	private void toggleStarred(Query q) {
		// toggle state
		q.setStarred(!q.isStarred());
		
		// save query
		final QueryManager qManager = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = qManager.createResultSetManager();
		try {
			rsManager.saveQuery(getProject(), q);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	/*
	 * Column renderers.
	 */
	private class DateRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel retVal = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			if(value instanceof GregorianCalendar) {
				final DateFormatter pdf = new DateFormatter();
				retVal.setText(pdf.format((DateTime)value));
			}
			
			return retVal;
		}
		
	};
	
	private class StarRenderer extends StarBox implements TableCellRenderer {
		
		public StarRenderer() {
			super(IconSize.SMALL);
			setBackground(Color.white);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			StarBox retVal = this;
			
			if(value != null && value instanceof Boolean) {
				Boolean val = (Boolean)value;
				retVal.setSelected(val);
				retVal.setEnabled(true);
			} else {
				retVal.setEnabled(false);
			}
			
			DefaultTableCellRenderer defaultRenderer = 
				new DefaultTableCellRenderer();
			JComponent c = (JComponent)defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			retVal.setBackground(c.getBackground());
			
			return retVal;
		}
	}
	
}

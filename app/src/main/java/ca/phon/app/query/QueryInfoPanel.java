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
package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import ca.phon.app.query.report.ReportWizard;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.DateFormatter;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.StarBox;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Display information about a given query.
 *
 */
public class QueryInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 7694431501108852083L;

	private final static Logger LOGGER = Logger
			.getLogger(QueryInfoPanel.class.getName());
	
	private StarBox starBox;
	
	/**
	 * Name label
	 */
	private JXLabel nameLabel;
	private JTextField nameField;
	
	/**
	 * Comments label
	 */
	private JTextArea commentsArea;
	
	/**
	 * Edit/open buttons
	 */
	private JButton editButton;
	private JButton openButton;
	
	/**
	 * Results table
	 */
	private JXBusyLabel busyLabel;
	private JXTable resultsTable;
	private TableRowSorter<ResultSetTableModel> resultsRowSorter;
	private ResultSetTableModel resultsModel;
	private JCheckBox hideResultsBox;
	private JCheckBox openEditorBox;
	private JButton reportButton;
	
	/**
	 * Section panels
	 */
	private JPanel infoSection;
	private JPanel resultsSection;
	
	/**
	 * Date label
	 */
	private JLabel dateLabel;
	
	/**
	 * Uuid label
	 */
	private JLabel uuidLabel;
	
	/**
	 * Project
	 */
	private Project project;
	
	/**
	 * Query
	 */
	private Query query;
	
	/**
	 * Construction
	 */
	public QueryInfoPanel(Project project) {
		super();
		
		this.project = project;
		
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		starBox = new StarBox(IconSize.SMALL);
		starBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleStarred();
			}
		});
		
		// query name
		nameLabel = new JXLabel();
		Font nameFont = nameLabel.getFont().deriveFont(Font.BOLD, 14.0f);
		nameLabel.setFont(nameFont);
		
		final ImageIcon searchIcon = 
				IconManager.getInstance().getIcon("actions/system-search", IconSize.SMALL);
		final PhonUIAction openAction = 
				new PhonUIAction(this, "onOpenQuery");
		openAction.putValue(PhonUIAction.NAME, "Open Query");
		openAction.putValue(PhonUIAction.SMALL_ICON, searchIcon);
		openAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open query in editor");
		openButton = new JButton(openAction);
		
		nameField = new JTextField();
		nameField.setFont(nameFont);
		
		uuidLabel = new JLabel();
		
		dateLabel = new JLabel();
		
		commentsArea = new JTextArea();
		commentsArea.setRows(5);
		commentsArea.setLineWrap(true);
		commentsArea.setEditable(false);
		commentsArea.setFont(Font.getFont("dialog"));
		final JScrollPane commentsLabelScroller = new JScrollPane(commentsArea);
		
		// layout form components
		final FormLayout layout = new FormLayout(
				"right:pref, 3dlu, fill:pref:grow, right:pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, top:pref, fill:pref:grow");
		final CellConstraints cc = new CellConstraints();
		
		infoSection = new JPanel(layout);
		infoSection.setBorder(BorderFactory.createTitledBorder("Information"));
		infoSection.add(starBox, cc.xy(1, 1));
		infoSection.add(nameLabel, cc.xy(3, 1));
		infoSection.add(openButton, cc.xy(4, 1));
		
		infoSection.add(new JLabel("UUID:"), cc.xy(1, 3));
		infoSection.add(uuidLabel, cc.xyw(3, 3, 2));
		
		infoSection.add(new JLabel("Date:"), cc.xy(1, 5));
		infoSection.add(dateLabel, cc.xyw(3, 5, 2));
		
		infoSection.add(new JLabel("Comments:"), cc.xy(1, 7));
		infoSection.add(commentsLabelScroller, cc.xywh(3, 7, 2, 2));
		
		resultsModel = new ResultSetTableModel(project, null);
		resultsModel.addPropertyChangeListener(bgTaskPropertyListener);
		resultsTable = new JXTable(resultsModel);
		resultsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsTable.addMouseListener(resultsMouseListener);
		resultsRowSorter = new TableRowSorter<ResultSetTableModel>(resultsModel);
		resultsRowSorter.setSortsOnUpdates(true);
		final RowSorter.SortKey sortKey = new RowSorter.SortKey(ResultSetTableModel.Columns.ID.ordinal(), SortOrder.ASCENDING);
		resultsRowSorter.setSortKeys(Collections.singletonList(sortKey));
		resultsTable.setRowSorter(resultsRowSorter);
		resultsTable.setColumnControlVisible(true);
		resultsTable.addHighlighter(HighlighterFactory.createSimpleStriping());
		resultsTable.setVisibleRowCount(10);

		// remove selection column
		resultsTable.getColumnModel().removeColumn(resultsTable.getColumn(0));
		JScrollPane resultsScroller = new JScrollPane(resultsTable);
		
		final ImageIcon reportIcon = IconManager.getInstance().getIcon(
				"mimetypes/x-office-spreadsheet", IconSize.SMALL);
		final PhonUIAction reportAction = new PhonUIAction(this, "onReport");
		reportAction.putValue(PhonUIAction.NAME, "Report");
		reportAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Create report");
		reportAction.putValue(PhonUIAction.SMALL_ICON, reportIcon);
		reportButton = new JButton(reportAction);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.setBusy(false);
		
		hideResultsBox = new JCheckBox("Hide empty result sets");
		hideResultsBox.setSelected(false);
		hideResultsBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleRowFilter();
			}
		});
		
		// system preference
		openEditorBox = new JCheckBox("Open session with result set");
		openEditorBox.setSelected(true);
		
		resultsSection = new JPanel(new BorderLayout());
		resultsSection.setBorder(BorderFactory.createTitledBorder("Results"));
		final FormLayout topLayout = new FormLayout(
				"pref, left:pref, left:pref, fill:pref:grow, right:pref", "pref");
		final JPanel topResultsPanel = new JPanel();
		topResultsPanel.setLayout(topLayout);
		topResultsPanel.add(busyLabel, cc.xy(1, 1));
		topResultsPanel.add(hideResultsBox, cc.xy(2, 1));
		topResultsPanel.add(openEditorBox, cc.xy(3, 1));
		topResultsPanel.add(reportButton, cc.xy(5, 1));
		
		resultsSection.add(topResultsPanel, BorderLayout.NORTH);
		resultsSection.add(resultsScroller, BorderLayout.CENTER);
		
		openButton.setEnabled(false);
		reportButton.setEnabled(false);
		
		add(infoSection, BorderLayout.NORTH);
		add(resultsSection, BorderLayout.CENTER);
	}
	
	private void toggleRowFilter() {
		if(hideResultsBox.isSelected()) {
			final RowFilter<ResultSetTableModel, Integer>
				filter = RowFilter.regexFilter("[1-9][0-9]*", ResultSetTableModel.Columns.ResultCount.ordinal());
			resultsRowSorter.setRowFilter(filter);
		} else {
			resultsRowSorter.setRowFilter(null);
		}
	}
	
	public void setQuery(Query query) {
		this.query = query;
		updateForm();
	}
	
	public void toggleStarred() {
		boolean starred = starBox.isSelected();
		if(query != null && query.isStarred() != starred) {
			query.setStarred(starred);
			final QueryManager qManager = QueryManager.getSharedInstance();
			final ResultSetManager rsManager = qManager.createResultSetManager();
			try {
				rsManager.saveQuery(project, query);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	public void onReport() {
		// open report wizard
		// get project from parent frame
		final CommonModuleFrame parentFrame = 
			(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, this);
		final Project project = parentFrame.getExtension(Project.class);
		
		if(parentFrame != null && project != null) {
			ReportWizard wizard = new ReportWizard(project, query);
			wizard.setParentFrame(parentFrame);
			wizard.pack();
			wizard.setLocationByPlatform(true);
			wizard.setVisible(true);
		}
	}
	
	public void onOpenQuery() {
		final QueryScript script = new QueryScript(query.getScript().getSource());
		ScriptParameters params = new ScriptParameters();
		try {
			params = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		for(ScriptParam sp:params) {
			for(String id:sp.getParamIds()) {
				Object v = query.getScript().getParameters().get(id);
				if(v != null) {
					sp.setValue(id, v);
				}
			}
		}
		
		final QueryName qn = new QueryName(query.getName());
		script.putExtension(QueryName.class, qn);
		
		QueryEditorWindow sd = 
			new QueryEditorWindow("Script Editor", project,
					script);
		sd.pack();
		sd.setLocationByPlatform(true);
		sd.setVisible(true);
	}
	
	public void updateForm() {
		final DateFormatter dateFormatter = new DateFormatter();
		
		if(query != null) {
			nameLabel.setText(query.getName());
			uuidLabel.setText(query.getUUID().toString());
			dateLabel.setText(dateFormatter.format(query.getDate()));
			commentsArea.setText(query.getComments());
			commentsArea.setCaretPosition(0);
			starBox.setSelected(query.isStarred());
			
			openButton.setEnabled(true);
			reportButton.setEnabled(true);
		} else {
			nameLabel.setText("");
			uuidLabel.setText("");
			dateLabel.setText("");
			commentsArea.setText("");
			starBox.setSelected(false);
			
			openButton.setEnabled(false);
			reportButton.setEnabled(false);
		}
		resultsModel.setQuery(query);
	}
	
	/**
	 * Open the given result set in a new window.
	 * 
	 * @param rs
	 */
	public void openResultSet(ResultSet rs) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", project);
		initInfo.put("query", query);
		initInfo.put("resultset", rs);
		initInfo.put("opensession", openEditorBox.isSelected());
		
		// open editor first....
		try {
			PluginEntryPointRunner.executePlugin("ResultSetViewer", initInfo);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public void deleteResultSet(ResultSet rs) {
		String msg = "Delete result set '" + 
				rs.getSessionPath() + "' for query '" + query.getName() + "'?  Action cannot be undone.";
		int result = NativeDialogs.showOkCancelDialogBlocking(
				CommonModuleFrame.getCurrentFrame(), null, "Delete result set", msg);
		if(result == 0) {
			final QueryManager qManager = QueryManager.getSharedInstance();
			final ResultSetManager rsManager = qManager.createResultSetManager();
			
			try {
				rsManager.deleteResultSet(project, query, rs);
				resultsModel.removeResultSet(rs);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * Listener for background tasks on the table model
	 */
	private final PropertyChangeListener bgTaskPropertyListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(!evt.getPropertyName().equals(ResultSetTableModel.RUNNING_IN_BACKGROUND))
				return;
			final Boolean busy = (Boolean)evt.getNewValue();
			final Runnable setBusy = new Runnable() {
				
				@Override
				public void run() {
					busyLabel.setBusy(busy);
				}
			};
			SwingUtilities.invokeLater(setBusy);
		}
	};
	
	/**
	 * Mouse listener for the result table
	 */
	private final MouseListener resultsMouseListener = new MouseInputAdapter() {
		@Override
		public void mouseClicked(MouseEvent me) {
			// handle double clicks
			if(me.getClickCount() == 2 && me.getButton() == MouseEvent.BUTTON1) {
				int selectedRow = resultsTable.getSelectedRow();
				if(selectedRow < 0) return;
				selectedRow = resultsTable.convertRowIndexToModel(selectedRow);
				final ResultSet rs = resultsModel.resultSetForRow(selectedRow);
				if(rs != null) {
					openResultSet(rs);
				}
			}
		}
		
		@Override
		public void mousePressed(MouseEvent me) {
			if(me.isPopupTrigger()) {
				int row = resultsTable.rowAtPoint(me.getPoint());
				if(row >= 0)
					resultsTable.getSelectionModel().setSelectionInterval(row, row);
				
				showResultSetContextMenu(me);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger()) {
				int row = resultsTable.rowAtPoint(me.getPoint());
				if(row >= 0)
					resultsTable.getSelectionModel().setSelectionInterval(row, row);
				
				showResultSetContextMenu(me);
			}
		}
		
		private void showResultSetContextMenu(MouseEvent me) {
			int selectedRow = resultsTable.getSelectedRow();
			if(selectedRow < 0) return;
			selectedRow = resultsTable.convertRowIndexToModel(selectedRow);
			final ResultSet rs = resultsModel.resultSetForRow(selectedRow);
			
			final PhonUIAction openAct = new PhonUIAction(QueryInfoPanel.this, "openResultSet", rs);
			openAct.putValue(PhonUIAction.NAME, "Open");
			openAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open result set");
			final JMenuItem openItem = new JMenuItem(openAct);
			
			final PhonUIAction deleteAct = new PhonUIAction(QueryInfoPanel.this, "deleteResultSet", rs);
			deleteAct.putValue(PhonUIAction.NAME, "Delete");
			deleteAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete result set");
			final JMenuItem deleteItem = new JMenuItem(deleteAct);
			
			final JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.add(openItem);
			popupMenu.addSeparator();
			popupMenu.add(deleteItem);
			
			popupMenu.show(resultsTable, me.getX(), me.getY());
		}
	};
}

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.joda.time.DateTime;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.app.query.EditQueryDialog.ReturnStatus;
import ca.phon.app.query.report.ReportWizard;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.db.Script;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptContext;
import ca.phon.query.script.QueryTask;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.Session;
import ca.phon.session.SessionLocation;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;
import ca.phon.xml.XMLConverters;

public class QueryRunnerPanel extends JPanel {
	
	private final static Logger LOGGER = Logger.getLogger(QueryRunnerPanel.class.getName());
	
	/* UI Elements */
	/** The report button */
	private JButton reportButton;
	
	/** The save results button */
	private JButton saveButton;
	
	private JXBusyLabel busyLabel;
	
	/** The table */
	private JXTable resultsTable;
	
	/**
	 * Table model
	 */
	private RunnerTableModel tableModel;
	
	/** Top panel */
	private JPanel topPanel;
	
	/** Completed Label */
	private JLabel completedLabel;
	
	/** Hide no-result rows */
	private JCheckBox hideRowsBox;
	private JCheckBox openEditorBox;
	
	/**
	 * Load from temporary folder or project folder?
	 */
	private boolean loadFromTemp = true;
	
	/** Temp project, used for saving result sets until they are 'saved' by the user */
	private Project tempProject;
	
	/** Project */
	private Project project;
	
	/** The query */
	private Query query;
	
	/**
	 * Query script
	 */
	private final QueryScript queryScript;
	
	private final TableRowSorter<RunnerTableModel> resultsTableSorter;
	
	/**
	 * Property change event that is sent when the query is 
	 * saved in history.
	 * 
	 */
	public final static String QUERY_SAVED_PROP = "_query_saved_";
	
	public QueryRunnerPanel(Project project, QueryScript script, List<SessionLocation> selectedSessions) {
		super();
		this.project = project;
		this.queryScript = script;
		this.tableModel = new RunnerTableModel(selectedSessions);
		resultsTableSorter = new TableRowSorter<QueryRunnerPanel.RunnerTableModel>(tableModel);
		
		final UUID uuid = UUID.randomUUID();
		try {
			final String tmpFolder = System.getProperty("java.io.tmpdir");
			final String tmpProjectFolder =
					tmpFolder + File.separator + "phon-" + Long.toHexString(uuid.getLeastSignificantBits());
//			tempProjectFile.mkdirs();
			final ProjectFactory factory = new ProjectFactory();
			tempProject = factory.createProject(new File(tmpProjectFolder));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		// top panel
		FormLayout topLayout = new FormLayout(
				"pref, 3dlu, left:pref, left:pref, fill:pref:grow, pref, right:pref",
				"pref");
		CellConstraints cc = new CellConstraints();
		topPanel = new JPanel(topLayout);
		
		saveButton = new JButton("Save results");
		ImageIcon saveIcon = 
				IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		saveButton.setIcon(saveIcon);
		saveButton.setEnabled(false);
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showSaveQueryDialog();
			}
			
		});
		
		reportButton = new JButton("Report");
		ImageIcon ssIcon = 
			IconManager.getInstance().getIcon("mimetypes/x-office-spreadsheet", IconSize.SMALL);
		reportButton.setIcon(ssIcon);
		reportButton.setEnabled(false);
		reportButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				generateReport();
			}
			
		});
		reportButton.setEnabled(false);
		reportButton.setVisible(true);
		
		hideRowsBox = new JCheckBox("Hide empty results");
		hideRowsBox.setEnabled(false);
		hideRowsBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(hideRowsBox.isSelected()) {
					final RowFilter<RunnerTableModel, Integer>
						filter = RowFilter.regexFilter("[1-9][0-9]*", 3);
					resultsTableSorter.setRowFilter(filter);
				} else {
					resultsTableSorter.setRowFilter(null);
				}
			}
			
		});
		
		openEditorBox = new JCheckBox("Open session with results");
		openEditorBox.setSelected(true);
		openEditorBox.setVisible(false);
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.setBusy(true);
		
		String labelText = "Completed: 0/" + tableModel.getRowCount();
		completedLabel = new JLabel(labelText);

//		topPanel.add(cancelButton, cc.xy(6,1));
		topPanel.add(completedLabel, cc.xy(3,1));
		topPanel.add(busyLabel, cc.xy(1, 1));
		topPanel.add(openEditorBox, cc.xy(4, 1));
		topPanel.add(saveButton, cc.xy(6, 1));
		topPanel.add(reportButton, cc.xy(7, 1));
		
		// table
		
		resultsTable = new JXTable(tableModel);
		resultsTable.addHighlighter(HighlighterFactory.createSimpleStriping());
		resultsTable.setRowSorter(resultsTableSorter);
//		resultsTable.setRowSorter(queryTaskTableSorter);
		
		resultsTable.getColumn(1).setCellRenderer(statusCellRenderer);
		resultsTable.getColumn(2).setCellRenderer(progressCellRenderer);
//		resultsTable.getColumn(3).setCellRenderer(srRenderer);
		
		resultsTable.setColumnControlVisible(true);
		resultsTable.setVisibleRowCount(15);
		resultsTable.packAll();
		
		resultsTable.addMouseListener(tableMouseListener);
		
		add(new JScrollPane(resultsTable), BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
	}
	
	public void startQuery() {
		final PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		worker.invokeLater(queryTask);
		worker.start();
	}
	
	public void stopQuery() {
		queryTask.shutdown();
	}
	
	public boolean isRunning() {
		return queryTask.getStatus() == TaskStatus.RUNNING;
	}
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
	
	public boolean isSaved() {
		return !loadFromTemp;
	}
	
	private int numberComplete = 0;
	private void taskCompleted() {
		numberComplete++;
		completedLabel.setText("Completed: " + numberComplete + "/" + tableModel.getRowCount());
	}

	/**
	 * Show ui for editing query details.
	 */
	private void showSaveQueryDialog() {
		final EditQueryDialog queryDialog = new EditQueryDialog(getProject(), getQuery());
		queryDialog.setLocationRelativeTo(this);
		if(queryDialog.showModal() == ReturnStatus.OK)
			saveQuery();
	}
	
	private void generateReport() {
		// get project from parent frame
		final CommonModuleFrame parentFrame = 
			(CommonModuleFrame)SwingUtilities.getAncestorOfClass(CommonModuleFrame.class, this);
		final ProjectFrameExtension pfe = parentFrame.getExtension(ProjectFrameExtension.class);
		
		if(parentFrame != null && pfe != null && pfe.getProject() != null) {
			ReportWizard wizard = null;
			if(loadFromTemp) {
				wizard = new ReportWizard(getProject(), tempProject, query);
			} else {
				wizard = new ReportWizard(getProject(), query);
			}
			wizard.setParentFrame(parentFrame);
			wizard.pack();
			wizard.setLocationByPlatform(true);
			wizard.setVisible(true);
		}
	}
	
	/**
	 * Perform save query.
	 */
	private void saveQuery() {
		final Runnable beginningTask = new Runnable() {
			@Override
			public void run() {
				busyLabel.setBusy(true);
				saveButton.setEnabled(false);
			}
		};
		
		final Runnable successTask = new Runnable() {
			
			@Override
			public void run() {
				saveButton.setVisible(false);
			}
		};
		final Runnable finalTask = new Runnable() {
			@Override
			public void run() {
				saveButton.setEnabled(true);
				busyLabel.setBusy(false);
				
			}
		};
		
		// place save task on background thread
		final Runnable saveTask = new Runnable() { public void run() {
			SwingUtilities.invokeLater(beginningTask);
			final QueryManager qManager = QueryManager.getSharedInstance();
			final ResultSetManager rsManager = qManager.createResultSetManager();
			
			// save query first
			try {
				rsManager.saveQuery(getProject(), getQuery());
				
				// load from temp project
				for(SessionLocation sessionLocation:tableModel.sessions) {
					final String sessionName = sessionLocation.getCorpus() + "." + sessionLocation.getSession();
					final ResultSet tempResults = rsManager.loadResultSet(tempProject, getQuery(), sessionName);
					
					// save to project
					rsManager.saveResultSet(getProject(), getQuery(), tempResults);
				}
				
				loadFromTemp = false;
				
				SwingUtilities.invokeLater(successTask);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} finally {
				SwingUtilities.invokeLater(finalTask);
			}
		}};
		PhonWorker.getInstance().invokeLater(saveTask);
		
		super.firePropertyChange(QUERY_SAVED_PROP, Boolean.FALSE, Boolean.TRUE);
	}
	
	private final PhonTask queryTask = new PhonTask() {

		@Override
		public void performTask() {
			// do nothing if already shutdown
			if(isShutdown()) return;
			super.setStatus(TaskStatus.RUNNING);
			
			final QueryManager qm = QueryManager.getSharedInstance();
			final QueryFactory qfactory = qm.createQueryFactory();
			final ResultSetManager rsManager = qm.createResultSetManager();
			
			// setup query object
			query = qfactory.createQuery(project);
			final QueryScriptContext ctx = queryScript.getQueryContext();
			ScriptParameters scriptParams = new ScriptParameters();
			try {
				scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			
			final Script qScript = query.getScript();
			qScript.setSource(queryScript.getScript());
			final Map<String, String> sparams = new HashMap<String, String>();
			for(ScriptParam sp:scriptParams) {
				if(sp.hasChanged()) {
					for(String paramid:sp.getParamIds()) {
						sparams.put(paramid, sp.getValue(paramid).toString());
					}
				}
			}
			qScript.setParameters(sparams);
			qScript.setMimeType("text/javascript");
			
			query.setDate(DateTime.now());
			
			final QueryName queryName = queryScript.getExtension(QueryName.class);
			String name = (queryName != null ? queryName.getName() : "untitled");
//			if(queryName.indexOf('.') > 0) {
//				queryName = queryName.substring(0, queryName.lastIndexOf('.'));
//			}
			query.setName(name);
			
			try {
				rsManager.saveQuery(tempProject, query);
			} catch (IOException e1) {
				e1.printStackTrace();
				LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			}
			
			busyLabel.setBusy(true);
			
//			final QueryTask queryTask = new QueryTask(project, queryScript);
//			queryTask.addTaskListener(queryTaskListener);
			for(SessionLocation sessionLocation:tableModel.sessions) {
				if(isShutdown()) break;
				// load session
				try {
					final Session session = 
							project.openSession(sessionLocation.getCorpus(), sessionLocation.getSession());
					
					final QueryTask queryTask = new QueryTask(project, session, queryScript);
					queryTask.addTaskListener(queryTaskListener);
					
					queryTask.run();
					taskCompleted();
					
					rsManager.saveResultSet(tempProject, query, queryTask.getResultSet());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			busyLabel.setBusy(false);
			saveButton.setEnabled(true);
			reportButton.setEnabled(true);
			
			topPanel.remove(completedLabel);
			topPanel.add(hideRowsBox, (new CellConstraints()).xy(3,1));
			openEditorBox.setVisible(true);
			topPanel.revalidate();
			hideRowsBox.setEnabled(true);
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	};
	
	private MouseInputAdapter tableMouseListener = new MouseInputAdapter() {

		/* (non-Javadoc)
		 * @see javax.swing.event.MouseInputAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				int selectedIndex = resultsTable.getSelectedRow();
				selectedIndex = resultsTable.convertRowIndexToModel(selectedIndex);
				if(selectedIndex >= 0) {
					final SessionLocation location = tableModel.sessions.get(selectedIndex);
//					if(sessionNameObj == null) return;
					
					final String sessionName = location.getCorpus() + "." + location.getSession();
					final QueryManager qm = QueryManager.getSharedInstance();
					final ResultSetManager rsManager = qm.createResultSetManager();
					
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					try {
						final ResultSet rs = rsManager.loadResultSet((loadFromTemp ? tempProject : project), query, sessionName);
						initInfo.put("resultset", rs);
					} catch (IOException e1) {
						LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
					}
					initInfo.put("project", project);
					
					if(loadFromTemp) {
						initInfo.put("tempProject", tempProject);
					}
					initInfo.put("query", query);
					initInfo.put("opensession", openEditorBox.isSelected());
					
					// open editor first....
					try {
						PluginEntryPointRunner.executePlugin("ResultSetViewer", initInfo);
					} catch (PluginException ex) {
						LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					}
				}
			}
		}
		
	};
	
	private final PhonTaskListener queryTaskListener = new PhonTaskListener() {

		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
			final QueryTask queryTask = (QueryTask)task;
			final Session session = queryTask.getSession();
			
			final SessionLocation location = new SessionLocation(session.getCorpus(), session.getName());
			final int rowIdx = tableModel.sessions.indexOf(location);
			tableModel.setValueAt(newStatus, rowIdx, 1);
			
			if(newStatus == TaskStatus.RUNNING) {
				boolean autoScrollToRow = 
						(resultsTable.getSelectedRow() < 0)
						||
						(resultsTable.getSelectedRow() > 0 && !resultsTable.hasFocus());
				
				if(autoScrollToRow) {
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							resultsTable.scrollRowToVisible(rowIdx);
						}
					});
					
				}
			} else {
				// update results row
				tableModel.setValueAt(queryTask.getProperty(PhonTask.PROGRESS_PROP), rowIdx, 2);
				tableModel.setValueAt(queryTask.getResultSet().size(), rowIdx, 3);
			}
		}

		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			final QueryTask queryTask = (QueryTask)task;
			final Session session = queryTask.getSession();
			
			final SessionLocation location = new SessionLocation(session.getCorpus(), session.getName());
			final int rowIdx = tableModel.sessions.indexOf(location);
			
			if(property.equals(QueryTask.PROGRESS_PROP)) {
				tableModel.setValueAt((Integer)queryTask.getProperty(QueryTask.PROGRESS_PROP), rowIdx, 2);

				int size = (queryTask.getResultSet() != null ? queryTask.getResultSet().size() : 0);
				tableModel.setValueAt(size, rowIdx, 3);
			}
		}
		
	};

	/**
	 * Table model for results
	 */
	private class RunnerTableModel extends AbstractTableModel {
		
		private final List<SessionLocation> sessions;
		
		/*
		 * cached values, set by using setValueAt
		 */
		private final Map<SessionLocation, PhonTask.TaskStatus> statusMap
			= new HashMap<SessionLocation, PhonTask.TaskStatus>();
		
		private final Map<SessionLocation, Integer> progressMap
			= new HashMap<SessionLocation, Integer>();
		
		private final Map<SessionLocation, Integer> resultsMap
			= new HashMap<SessionLocation, Integer>();
		
		public RunnerTableModel(List<SessionLocation> selectedSessions) {
			sessions = new ArrayList<SessionLocation>();
			sessions.addAll(selectedSessions);
			Collections.sort(sessions);
		}
		
		/**
		 * column names
		 */
		private final String[] colNames = {
				"Session", "Status", "Progress", "# of Results"
		};

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public String getColumnName(int col) {
			return colNames[col];
		}
		
		@Override
		public int getRowCount() {
			return sessions.size();
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			Class<?> retVal = Object.class;
			
			switch(col) {
			case 0:
				retVal = SessionLocation.class;
				break;
				
			case 1:
				retVal = PhonTask.TaskStatus.class;
				break;
				
			case 2:
				retVal = Integer.class;
				break;
				
			case 3:
				retVal = Integer.class;
				break;
			}
			
			return retVal;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object retVal = null;
			final SessionLocation session = sessions.get(row);
			
			switch(col) {
			case 0:
				retVal = session;
				break;
				
			case 1:
				synchronized(statusMap) {
					PhonTask.TaskStatus status = statusMap.get(session);
					if(status == null) status = TaskStatus.WAITING;
					retVal = status;
				}
				break;
				
			case 2:
				synchronized(progressMap) {
					Integer progress = progressMap.get(session);
					if(progress == null) progress = 0;
					retVal = progress;
				}
				break;
				
			case 3:
				synchronized(resultsMap) {
					Integer results = resultsMap.get(session);
					if(results == null) results = 0;
					retVal = results;
				}
				break;
				
			default:
				retVal = "";
				break;
			}
			
			return retVal;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			final SessionLocation session = sessions.get(rowIndex);
			
			switch(columnIndex) {
			case 1:
				if(aValue instanceof PhonTask.TaskStatus) {
					final PhonTask.TaskStatus status = (PhonTask.TaskStatus)aValue;
					synchronized(statusMap) {
						statusMap.put(session, status);
					}
					fireCellUpdate(rowIndex, columnIndex);
				}
				break;
				
			case 2:
				if(aValue instanceof Integer) {
					final Integer progress = (Integer)aValue;
					synchronized(progressMap) {
						progressMap.put(session, progress);
					}
					fireCellUpdate(rowIndex, columnIndex);
				}
				break;
				
			case 3:
				if(aValue instanceof Integer) {
					final Integer results = (Integer)aValue;
					synchronized(resultsMap) {
						resultsMap.put(session, results);
					}
					fireCellUpdate(rowIndex, columnIndex);
				}
				break;
				
			default:
				break;
			}
		}
		
		private void fireCellUpdate(final int row, final int col) {
			final Runnable toRun = new Runnable() {
				@Override
				public void run() {
					fireTableCellUpdated(row, col);
				}
			};
			SwingUtilities.invokeLater(toRun);
		}

//		@Override
//		public boolean isCellEditable(int row, int col) {
//			return col != 0;
//		}
		
	}
	
	/* Cell renderers */
	private final DefaultTableCellRenderer statusCellRenderer = new DefaultTableCellRenderer() {
		
		ImageIcon waitingIcon = 
			IconManager.getInstance().getIcon("actions/free_icon", IconSize.SMALL);
		
		ImageIcon runningIcon = 
			IconManager.getInstance().getIcon("actions/greenled", IconSize.SMALL);
		
		ImageIcon errorIcon =
			IconManager.getInstance().getIcon("status/dialog-error", IconSize.SMALL);
		
		ImageIcon finishedIcon =
			IconManager.getInstance().getIcon("actions/ok", IconSize.SMALL);
		
		ImageIcon terminatedIcon =
			IconManager.getInstance().getIcon("status/dialog-warning", IconSize.SMALL);

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel retVal = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			TaskStatus status = (TaskStatus)value;
			
			if(status == TaskStatus.WAITING) {
				retVal.setIcon(waitingIcon);
			} else if(status == TaskStatus.RUNNING) {
				retVal.setIcon(runningIcon);
			} else if(status == TaskStatus.ERROR) {
				retVal.setIcon(errorIcon);
			} else if(status == TaskStatus.TERMINATED) {
				retVal.setIcon(terminatedIcon);
			} else if(status == TaskStatus.FINISHED) {
				retVal.setIcon(finishedIcon);
			}
			
			return retVal;
		}
		
	};
	
	private class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if(!(value instanceof Integer)) {
				setValue(0);
				return this;
			}
			
			Integer v = (Integer)value;
			
			this.setIndeterminate(false);
			this.setMinimum(0);
			this.setMaximum(100);
			this.setValue(v);
			
			Dimension d = this.getPreferredSize();
			d.setSize(d.getWidth(), 10);
			this.setPreferredSize(d);
			
			return this;
		}
		
	}
	private final ProgressCellRenderer progressCellRenderer = new ProgressCellRenderer();
}

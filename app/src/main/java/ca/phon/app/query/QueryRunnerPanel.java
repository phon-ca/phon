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

import ca.phon.app.log.*;
import ca.phon.app.project.ShadowProject;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.query.db.*;
import ca.phon.query.script.*;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.*;
import ca.phon.session.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;
import com.jgoodies.forms.layout.CellConstraints;
import org.apache.commons.lang.WordUtils;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class QueryRunnerPanel extends JPanel {

	private JXBusyLabel busyLabel;
	
	private JLabel completedLabel;
	
	/** The table */
	private JXTable resultsTable;
	
	/**
	 * Table model
	 */
	private RunnerTableModel tableModel;
	
	/** Top panel */
	private JPanel topPanel;
	
	/** Hide no-result rows */
	private JCheckBox hideRowsBox;
	
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
	
	private boolean includeExcluded;
	
	/**
	 * Query script
	 */
	private final QueryScript queryScript;
	
	private final TableRowSorter<RunnerTableModel> resultsTableSorter;

	private final AtomicReference<PhonWorkerGroup> workerGroupRef = new AtomicReference<>();

	private final AtomicReference<CountDownLatch> taskLatchRef = new AtomicReference<>();

	private final AtomicReference<TaskStatus> taskStatusRef = new AtomicReference<>(TaskStatus.WAITING);
	
	/**
	 * Property change event that is sent when the query is 
	 * saved in history.
	 * 
	 */
	public final static String QUERY_SAVED_PROP = "_query_saved_";
	
	public QueryRunnerPanel(Project project, QueryScript script, List<SessionPath> selectedSessions, boolean includeExcluded) {
		super();
		this.project = project;
		this.queryScript = script;
		this.tableModel = new RunnerTableModel(selectedSessions);
		this.includeExcluded = includeExcluded;
		resultsTableSorter = new TableRowSorter<QueryRunnerPanel.RunnerTableModel>(tableModel);
		
		try {
			tempProject = ShadowProject.of(project);
		} catch (ProjectConfigurationException e) {
			LogUtil.severe( e.getLocalizedMessage(), e);
		}
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		// top panel
		CellConstraints cc = new CellConstraints();
		topPanel = new JPanel(new BorderLayout());
		
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
		
		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.setBusy(true);
		
		String labelText = "0/" + tableModel.getRowCount();
		completedLabel = new JLabel(labelText);
		
		busyLabel.setText(labelText);
	
		JPanel leftPanel = new JPanel(new HorizontalLayout(5));
		leftPanel.add(busyLabel);
		leftPanel.add(completedLabel);
		topPanel.add(leftPanel,BorderLayout.WEST);
		
		topPanel.add(hideRowsBox,BorderLayout.CENTER);
		
		// table
		resultsTable = new JXTable(tableModel);
		resultsTable.addHighlighter(HighlighterFactory.createSimpleStriping());
		resultsTable.setRowSorter(resultsTableSorter);
		
		resultsTable.getColumn(1).setCellRenderer(statusCellRenderer);
		resultsTable.getColumn(2).setCellRenderer(progressCellRenderer);
		
		resultsTable.setColumnControlVisible(true);
		resultsTable.setVisibleRowCount(15);
		resultsTable.packAll();
		
		resultsTable.addMouseListener(tableMouseListener);
		
		add(new JScrollPane(resultsTable), BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
	}
		
	public void startQuery() {
		setStatus(TaskStatus.RUNNING);
		SwingUtilities.invokeLater(() -> busyLabel.setBusy(true));

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
			LogUtil.severe( e.getLocalizedMessage(), e);
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

		query.setDate(LocalDateTime.now());

		final QueryName queryName = queryScript.getExtension(QueryName.class);
		String name = (queryName != null ? queryName.getName() : "untitled");
		query.setName(name);

		try {
			rsManager.saveQuery(tempProject, query);
		} catch (IOException e1) {
			e1.printStackTrace();
			LogUtil.severe( e1.getLocalizedMessage(), e1);
		}

		int numProcessors = Runtime.getRuntime().availableProcessors();
		int numThreads = (int)Math.ceil((float)numProcessors / 4.0);
		int numSessions = tableModel.getRowCount();
		final PhonWorkerGroup workerGroup = new PhonWorkerGroup(Math.min(numThreads, numSessions));
		workerGroupRef.set(workerGroup);
		workerGroup.begin();

		final CountDownLatch taskCountDownLatch = new CountDownLatch(tableModel.sessions.size());
		taskLatchRef.set(taskCountDownLatch);
		int serial = 0;
		for(SessionPath sessionLocation:tableModel.sessions) {
			// load session
			try {
				final String bufferName = query.getName() + ":" +
						sessionLocation.toString();

				final Session session =
						project.openSession(sessionLocation.getCorpus(), sessionLocation.getSession());

				final QueryScript clonedScript = (QueryScript)queryScript.clone();
				final QueryTask queryTask = new QueryTask(project, session, clonedScript, serial++);
				queryTask.setIncludeExcludedRecords(includeExcluded);
				queryTask.addTaskListener(queryTaskListener);
				queryTask.addTaskListener(new PhonTaskListener() {
					@Override
					public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
						if(newStatus == TaskStatus.FINISHED) {
							try {
								synchronized (rsManager) {
									rsManager.saveResultSet(tempProject, query, queryTask.getResultSet());
								}
							} catch (IOException e) {
								LogUtil.warning(e);
							}
							taskCompleted();
							taskLatchRef.get().countDown();
						}
					}

					@Override
					public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {

					}

				});

				workerGroup.queueTask(queryTask);
			} catch (IOException e) {
				LogUtil.severe( e.getLocalizedMessage(), e);
			}
		}

		workerGroup.queueTask(() -> {
			try {
				taskCountDownLatch.await();
				SwingUtilities.invokeLater(() -> {
					final PrintStream bufferOut = queryScript.getQueryContext().getStdOut();
					bufferOut.flush();
					bufferOut.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
					bufferOut.flush();

					busyLabel.setBusy(false);

					hideRowsBox.setEnabled(true);
					topPanel.add(hideRowsBox, BorderLayout.CENTER);
				});
				workerGroup.shutdown();

				setStatus(TaskStatus.FINISHED);
			} catch (InterruptedException e) {
				LogUtil.severe(e);
			}
		});

//		workerGroup.begin();
	}
	
	public void stopQuery() {
		if(isRunning()) {
			workerGroupRef.get().shutdown();
			for(int i = 0; i < tableModel.getRowCount(); i++) {
				final TaskStatus currentStatus = (TaskStatus) tableModel.getValueAt(i, 1);
				if(currentStatus == TaskStatus.WAITING) {
					tableModel.setValueAt(TaskStatus.TERMINATED, i, 1);
					if(taskLatchRef.get() != null) {
						taskLatchRef.get().countDown();
					}
				}
			}
			SwingUtilities.invokeLater(() -> busyLabel.setBusy(false));
			setStatus(TaskStatus.TERMINATED);
		}
	}

	private void setStatus(TaskStatus taskStatus) {
		final TaskStatus currentStatus = getStatus();
		this.taskStatusRef.set(taskStatus);
		firePropertyChange("taskStatus", currentStatus, taskStatus);
	}

	public TaskStatus getStatus() {
		return this.taskStatusRef.get();
	}

	public boolean hasStarted() {
		return getStatus() != TaskStatus.WAITING;
	}

	public boolean isRunning() {
		return hasStarted() && getStatus() == TaskStatus.RUNNING;
	}
	
	public Project getProject() {
		return project;
	}
	
	/**
	 * Temporary project folder used to store query results.
	 * 
	 * @return
	 */
	public Project getTempProject() {
		return this.tempProject;
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
		completedLabel.setText(numberComplete + "/" + tableModel.getRowCount());
		super.firePropertyChange("numberComplete", numberComplete-1, numberComplete);
	}
	
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
					final SessionPath location = tableModel.sessions.get(selectedIndex);

					final String sessionName = location.getCorpus() + "." + location.getSession();
					final QueryManager qm = QueryManager.getSharedInstance();
					final ResultSetManager rsManager = qm.createResultSetManager();
					
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					try {
						final ResultSet rs = rsManager.loadResultSet((loadFromTemp ? tempProject : project), query, sessionName);
						initInfo.put("resultset", rs);
					} catch (IOException e1) {
						LogUtil.severe( e1.getLocalizedMessage(), e1);
					}
					initInfo.put("project", project);
					
					if(loadFromTemp) {
						initInfo.put("tempProject", tempProject);
					}
					initInfo.put("query", query);

					PluginEntryPointRunner.executePluginInBackground(ResultSetEP.EP_NAME, initInfo);
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
			
			final SessionPath location = new SessionPath(session.getCorpus(), session.getName());
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
				int size = (queryTask.getResultSet() != null ? queryTask.getResultSet().size() : 0);
				tableModel.setValueAt(size, rowIdx, 3);
			}
		}

		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			final QueryTask queryTask = (QueryTask)task;
			final Session session = queryTask.getSession();
			
			final SessionPath location = new SessionPath(session.getCorpus(), session.getName());
			final int rowIdx = tableModel.sessions.indexOf(location);
			
			if(property.equals(QueryTask.PROGRESS_PROP)) {
				tableModel.setValueAt((int)Math.ceil((Float)queryTask.getProperty(QueryTask.PROGRESS_PROP)), rowIdx, 2);

				int size = (queryTask.getResultSet() != null ? queryTask.getResultSet().size() : 0);
				tableModel.setValueAt(size, rowIdx, 3);
			}
		}
		
	};

	/**
	 * Table model for results
	 */
	private class RunnerTableModel extends AbstractTableModel {
		
		private final List<SessionPath> sessions;
		
		/*
		 * cached values, set by using setValueAt
		 */
		private final Map<SessionPath, PhonTask.TaskStatus> statusMap
			= new HashMap<SessionPath, PhonTask.TaskStatus>();
		
		private final Map<SessionPath, Integer> progressMap
			= new HashMap<SessionPath, Integer>();
		
		private final Map<SessionPath, Integer> resultsMap
			= new HashMap<SessionPath, Integer>();
		
		public RunnerTableModel(List<SessionPath> selectedSessions) {
			sessions = new ArrayList<SessionPath>();
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
				retVal = SessionPath.class;
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
			final SessionPath session = sessions.get(row);
			
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
			final SessionPath session = sessions.get(rowIndex);
			
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
			
			retVal.setText(WordUtils.capitalize(retVal.getText().toLowerCase()));
			
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

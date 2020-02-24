/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
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

import org.apache.commons.lang.WordUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.jgoodies.forms.layout.CellConstraints;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogBuffer;
import ca.phon.app.project.ShadowProject;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.project.exceptions.ProjectConfigurationException;
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
import ca.phon.session.SessionPath;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

public class QueryRunnerPanel extends JPanel {

	private static final long serialVersionUID = 1427147887370979071L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryRunnerPanel.class.getName());

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
			LOGGER.error( e.getLocalizedMessage(), e);
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
		final PhonWorker worker = PhonWorker.createWorker();
		worker.setFinishWhenQueueEmpty(true);
		worker.invokeLater(queryTask);
		queryTask.addTaskListener(new PhonTaskListener() {
			
			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
				firePropertyChange("taskStatus", oldStatus, newStatus);
			}
			
			@Override
			public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
				
			}
		});
		worker.start();
		
	}
	
	public void stopQuery() {
		queryTask.shutdown();
	}
	
	public boolean isRunning() {
		return queryTask.getStatus() == TaskStatus.RUNNING;
	}
	
	public TaskStatus getTaskStatus() {
		return queryTask.getStatus();
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
				LOGGER.error( e.getLocalizedMessage(), e);
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
				LOGGER.error( e1.getLocalizedMessage(), e1);
			}
			
			busyLabel.setBusy(true);
			
			int serial = 0;
			for(SessionPath sessionLocation:tableModel.sessions) {
				if(isShutdown()) break;
				// load session
				try {
					final String bufferName = query.getName() + ":" + 
			        		sessionLocation.toString();
					
//					final AtomicReference<BufferWindow> buffersRef = new AtomicReference<BufferWindow>();
//					final AtomicReference<LogBuffer> logBufferRef = new AtomicReference<LogBuffer>();
//					
//					final Runnable onEdt = new Runnable() {
//						public void run() {
//							final BufferWindow buffers = BufferWindow.getInstance();
//							final LogBuffer logBuffer = buffers.createBuffer(bufferName).getLogBuffer();
//							buffersRef.set(buffers);
//							logBufferRef.set(logBuffer);
//						}
//					};
//					try {
//						SwingUtilities.invokeAndWait(onEdt);
//					} catch (InterruptedException e1) {
//						LOGGER.error( e1.getLocalizedMessage(),
//								e1);
//					} catch (InvocationTargetException e1) {
//						LOGGER.error( e1.getLocalizedMessage(),
//								e1);
//					}
					
//					if(logBufferRef.get() != null) {
//						final PrintStream outStream = new PrintStream(logBufferRef.get().getStdOutStream());
//				        ctx.redirectStdErr(new PrintStream(logBufferRef.get().getStdErrStream(), false, "UTF-8"));
//				        ctx.redirectStdOut(outStream);
//				        
//				        logBufferRef.get().getDocument().addDocumentListener(new DocumentListener() {
//							
//							@Override
//							public void removeUpdate(DocumentEvent e) {
//							}
//							
//							@Override
//							public void insertUpdate(DocumentEvent e) {
//								if(!buffersRef.get().isVisible()) {
//									buffersRef.get().showWindow();
//								}
//								buffersRef.get().selectBuffer(logBufferRef.get().getBufferName());
//								logBufferRef.get().getDocument().removeDocumentListener(this);
//							}
//							
//							@Override
//							public void changedUpdate(DocumentEvent e) {
//							}
//							
//						});
//				       
//				        outStream.flush();
//				        outStream.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_BUSY);
//				        outStream.flush();
//					}
					
					final Session session = 
							project.openSession(sessionLocation.getCorpus(), sessionLocation.getSession());
					
					final QueryTask queryTask = new QueryTask(project, session, queryScript, ++serial);
					queryTask.setIncludeExcludedRecords(includeExcluded);
					queryTask.addTaskListener(queryTaskListener);
					
					queryTask.run();
					taskCompleted();
					
//					if(logBufferRef.get() != null) {
//						if(logBufferRef.get().getText().length() == 0) {
//							buffersRef.get().removeBuffer(logBufferRef.get().getBufferName());
//						}
//						final PrintStream outStream = new PrintStream(logBufferRef.get().getStdOutStream());
//						outStream.flush();
//				        outStream.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.STOP_BUSY);
//				        outStream.flush();
//					}
					
					rsManager.saveResultSet(tempProject, query, queryTask.getResultSet());
				} catch (IOException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
			
			final PrintStream bufferOut = queryScript.getQueryContext().getStdOut();
			bufferOut.flush();
			bufferOut.print(LogBuffer.ESCAPE_CODE_PREFIX + BufferPanel.SHOW_TABLE_CODE);
			bufferOut.flush();
			
			busyLabel.setBusy(false);
//			saveButton.setEnabled(true);
//			reportButton.setEnabled(true);
			
			hideRowsBox.setEnabled(true);
			topPanel.add(hideRowsBox, BorderLayout.CENTER);
			
			if(getStatus() != TaskStatus.TERMINATED && getStatus() != TaskStatus.ERROR)
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
					final SessionPath location = tableModel.sessions.get(selectedIndex);
//					if(sessionNameObj == null) return;
					
					final String sessionName = location.getCorpus() + "." + location.getSession();
					final QueryManager qm = QueryManager.getSharedInstance();
					final ResultSetManager rsManager = qm.createResultSetManager();
					
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					try {
						final ResultSet rs = rsManager.loadResultSet((loadFromTemp ? tempProject : project), query, sessionName);
						initInfo.put("resultset", rs);
					} catch (IOException e1) {
						LOGGER.error( e1.getLocalizedMessage(), e1);
					}
					initInfo.put("project", project);
					
					if(loadFromTemp) {
						initInfo.put("tempProject", tempProject);
					}
					initInfo.put("query", query);
//					initInfo.put("opensession", openEditorBox.isSelected());
					
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

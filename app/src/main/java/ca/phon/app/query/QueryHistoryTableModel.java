/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.beans.*;
import java.util.*;

import javax.swing.table.*;

import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

public class QueryHistoryTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 2387204905112801347L;

	private Project project;

	public static enum Columns {
		Date,
		QueryName,
		NumberOfResultSets;
		
		String[] colNames = {
				"Date",
				"Name",
				"# of Result Sets"
		};
		
		Class<?>[] classes = {
				java.util.Calendar.class,
				String.class,
				Integer.class
				
		};
		
		public String getName() {
			return colNames[ordinal()];
		}
		
		public Class<?> getClazz() {
			return classes[ordinal()];
		}
	}
	
	/**
	 * Property support for table
	 */
	private final PropertyChangeSupport propertySupport = 
			new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propertySupport.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return propertySupport.getPropertyChangeListeners(propertyName);
	}

	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		propertySupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Property for threading status of model
	 */
	public final static String RUNNING_IN_BACKGROUND = "_running_in_background_";
	
	public final static String STARRED_ONLY = "_starred_only_";
	
	/**
	 * Query list
	 */
	private final List<Query> queryList = new ArrayList<Query>();
	
	private boolean starredOnly = false;
	
	public QueryHistoryTableModel(Project project) {
		super();
		
		this.project = project;
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}
	
	@Override
	public String getColumnName(int column) {
		return Columns.values()[column].getName();
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Columns.values()[column].getClass();
	}
	
	public TableCellRenderer getColumnRenderer(int column) {
		return new DefaultTableCellRenderer();
	}
	
	public Query getQueryForRow(int rowIndex) {
		Query retVal = null;
		synchronized (queryList) {
			if(rowIndex >= 0 && rowIndex < queryList.size()) {
				retVal = queryList.get(rowIndex);
			}
		}
		return retVal;
	}
	
	// returns all queries for the given project
	private Set<Query> getQueriesForProject(Project project) {
		final QueryManager qManager = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = qManager.createResultSetManager();
		final Set<Query> retVal = new HashSet<Query>();
		retVal.addAll(rsManager.getQueries(project));
		return retVal;
	}
	
	@Override
	public int getRowCount() {
		int retVal = 0;
		
		synchronized (queryList) {
			retVal = queryList.size();
		}
		
		return retVal;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex < 0 || rowIndex >= getRowCount()) return null;
		Query rowQuery = getQueryForRow(rowIndex);
		Object retVal = "";
		
		if(rowQuery != null) {
			if(columnIndex == Columns.QueryName.ordinal()) {
				retVal = rowQuery.getName();
			} else if(columnIndex == Columns.NumberOfResultSets.ordinal()) {
				final QueryManager qManager = QueryManager.getSharedInstance();
				final ResultSetManager rsManager = qManager.createResultSetManager();
				retVal = rsManager.getResultSetsForQuery(project, rowQuery).size();
			} else if(columnIndex == Columns.Date.ordinal()) {
				retVal = rowQuery.getDate();
			} 
		}
		
		return retVal;
	}

	private PhonTask currentTask = null;
	/**
	 * Update table
	 */
	public void update() {
		if(currentTask != null && currentTask.getStatus() == TaskStatus.RUNNING) {
			currentTask.shutdown();
		}
		currentTask = new UpdateTableTask();
		currentTask.addTaskListener(new PhonTaskListener() {
			
			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				final boolean running = newStatus == TaskStatus.RUNNING;
				firePropertyChange(RUNNING_IN_BACKGROUND, !running, running);
			}
			
			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
			}
		});
		PhonWorker.getInstance().invokeLater(currentTask);
	}
	
	/**
	 * Load result sets and update table when ready.
	 * 
	 */
	private class UpdateTableTask extends PhonTask {

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			int rowCount = getRowCount();
				if(rowCount > 0) {
				synchronized(queryList) {
					queryList.clear();
					
					fireTableRowsDeleted(0, rowCount-1);
				}
			}
			
			if(project != null) {
				final QueryManager qManager = QueryManager.getSharedInstance();
				final ResultSetManager rsManager = qManager.createResultSetManager();
				
				for(Query q:rsManager.getQueries(project)) {
					if(super.isShutdown()) {
						super.setStatus(TaskStatus.TERMINATED);
						break;
					}
					q.isStarred();
					
					if(starredOnly && !q.isStarred())
						continue;
					
					rowCount = getRowCount();
					synchronized(queryList) {
						queryList.add(q);
					}
					fireTableRowsInserted(rowCount, rowCount);
				}
			}
			
			if(getStatus() != TaskStatus.TERMINATED)
				setStatus(TaskStatus.FINISHED);
		}
		
	}

	public void removeQuery(Query q) {
		int qIdx = -1;
		synchronized(queryList) {
			qIdx = queryList.indexOf(q);
		}
		if(qIdx >= 0) {
			synchronized(queryList) {
				queryList.remove(qIdx);
			}
			super.fireTableRowsDeleted(qIdx, qIdx);
		}
	}
	
	public void setStarredOnly(boolean selected) {
		boolean oldVal = starredOnly;
		starredOnly = selected;
		firePropertyChange(STARRED_ONLY, oldVal, starredOnly);
	}
}

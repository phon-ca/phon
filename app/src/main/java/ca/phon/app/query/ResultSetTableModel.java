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

import java.beans.*;
import java.util.*;

import javax.swing.table.*;

import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.*;

/**
 * Implements a selectable table model.
 *
 */
public class ResultSetTableModel extends AbstractTableModel {
	
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
	
	private Project project;
	
	/** The query */
	private Query query;
	
	/** Selected searches */
	private Map<String, Boolean> selectedResultSets = new HashMap<String, Boolean>();
	
	private List<ResultSet> resultSetList = Collections.synchronizedList(new ArrayList<ResultSet>());
	
	private UpdateTableTask currentTask;
	
	public enum Columns {
		Selected("", Boolean.class),
		ID("Session", String.class),
		ResultCount("# of Results", Integer.class);
		
		private String colName;
		
		private Class<?> clazz;
		
		private Columns(String colName, Class<?> clazz) {
			this.colName = colName;
			this.clazz = clazz;
		}
		
		public String getName() {
			return colName;
		}
		
		public Class<?> getClazz() {
			return clazz;
		}
	}
	
	public ResultSetTableModel(Project project, Query q) {
		super();
		
		this.project = project;
		setQuery(q);
	}
	
	public void setQuery(Query q) {
		if(this.query == q) return;
		this.query = q;
		
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
	
	public Query getQuery() {
		return this.query;
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}

	@Override
	public int getRowCount() {
		int retVal = 0;
			retVal = resultSetList.size();
		return retVal;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object retVal = new String();
		ResultSet resultSet = resultSetForRow(rowIndex);
		
		if(columnIndex == Columns.Selected.ordinal()) {
			Boolean selected = selectedResultSets.get(resultSet.getSessionPath());
			if(selected == null) {
				selectedResultSets.put(resultSet.getSessionPath(), Boolean.FALSE);
				selected = Boolean.FALSE;
			}
			retVal = selected;
		} else if(columnIndex == Columns.ID.ordinal()) {
			retVal = resultSet.getSessionPath();
		} else if(columnIndex == Columns.ResultCount.ordinal()) {
			retVal = resultSet.size();
		}
		
		return retVal;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Columns.values()[columnIndex].getClazz();
	}

	@Override
	public String getColumnName(int column) {
		return Columns.values()[column].getName();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		boolean retVal = false;
		if(columnIndex == Columns.Selected.ordinal())
			retVal = true;
		return retVal;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		synchronized(resultSetList) {
			if(columnIndex == Columns.Selected.ordinal() && 
					value instanceof Boolean) {
				Boolean v = (Boolean)value;
				ResultSet resultSet = resultSetList.get(rowIndex);
				selectedResultSets.put(resultSet.getSessionPath(), v);
				super.fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

	public ResultSet[] getSelectedSearches() {
		List<ResultSet> retVal = new ArrayList<ResultSet>();
		
		synchronized(resultSetList) {
			for(int sIndex = 0; sIndex < resultSetList.size(); sIndex++) {
				ResultSet s = resultSetList.get(sIndex);
				String sid = s.getSessionPath();
				Boolean selected = selectedResultSets.get(sid);
				selected = (selected == null ? false : selected);
				if(selected)
					retVal.add(s);
			}
		}
		
		return retVal.toArray(new ResultSet[0]);
	}
	
	public ResultSet resultSetForRow(int row) {
		ResultSet retVal = null;
		
		synchronized(resultSetList) {
			retVal = resultSetList.get(row);
		}
		
		return retVal;
	}
	
	public void removeResultSet(ResultSet rs) {
		int rowIdx = -1;
		synchronized(resultSetList) {
			rowIdx = resultSetList.indexOf(rs);
			resultSetList.remove(rs);
		}
		if(rowIdx >= 0) {
			super.fireTableRowsDeleted(rowIdx, rowIdx);
		}
	}
	
	public void setSelected(ResultSet s, boolean v) {
		selectedResultSets.put(s.getSessionPath(), v);
		super.fireTableDataChanged();
	}

	public void selectAll(Boolean selected) {
		synchronized(resultSetList) {
			for(ResultSet s:resultSetList) {
				selectedResultSets.put(s.getSessionPath(), selected);
			}
		}
		super.fireTableDataChanged();
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
				synchronized(resultSetList) {
					selectedResultSets.clear();
					resultSetList.clear();
					
					fireTableRowsDeleted(0, rowCount-1);
				}
			}
			
			if(query != null) {
				final QueryManager qManager = QueryManager.getSharedInstance();
				final ResultSetManager rsManager = qManager.createResultSetManager();
				
				for(ResultSet rs:rsManager.getResultSetsForQuery(project, query)) {
					if(super.isShutdown()) {
						super.setStatus(TaskStatus.TERMINATED);
						break;
					}
					synchronized(resultSetList) {
						resultSetList.add(rs);
					}
				}
				resultSetList.sort( (rs1, rs2) -> rs1.getSessionPath().compareTo(rs2.getSessionPath()) );
				fireTableRowsInserted(0, getRowCount()-1);
			}
			
			if(getStatus() != TaskStatus.TERMINATED)
				setStatus(TaskStatus.FINISHED);
		}
		
	}
}

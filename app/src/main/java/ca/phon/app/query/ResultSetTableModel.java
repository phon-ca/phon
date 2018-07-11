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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

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

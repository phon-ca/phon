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

import java.util.*;

import javax.swing.table.AbstractTableModel;

import ca.phon.query.db.ResultSet;
import ca.phon.query.script.QueryTask;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

/**
 *
 */
public class QueryTaskTableModel extends AbstractTableModel {
	
	/** The tasks */
	private List<QueryTask> tasks = 
		Collections.synchronizedList(new ArrayList<QueryTask>());
	
	private TaskListener listener = new TaskListener();
	
	public QueryTaskTableModel() {
		super();
	}
	
	public QueryTaskTableModel(QueryTask[] tasks) {
		super();
		
		for(QueryTask t:tasks) {
			t.addTaskListener(listener);
			this.tasks.add(t);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 4;
	}
	
	@Override
	public String getColumnName(int col) {
		String retVal = "";
		
		if(col == 0) {
			retVal = "Name";
		} else if(col == 1) {
			retVal = "Status";
		} else if(col == 2) {
			retVal = "Progress";
		} else if(col == 3) {
			retVal = "Results";
		}
		
		return retVal;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> retVal = String.class;
		
		if(col == 1)
			retVal = PhonTask.TaskStatus.class;
		else if(col == 2)
			retVal = Integer.class;
		else if(col == 3)
			retVal = String.class;
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return tasks.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		QueryTask task = tasks.get(rowIndex);
		
		if(columnIndex == 0)
			return task.getName();
		else if(columnIndex == 1)
			return task.getStatus();
		else if(columnIndex == 2)
			return task.getProperty(QueryTask.PROGRESS_PROP);
		else if(columnIndex == 3) {
			ResultSet s = task.getResultSet();
			if(s != null)
				return "" + s.size();
			else 
				return "";
		} else
			return null;
	}

	private int getTaskIndex(PhonTask task) {
		for(int i = 0; i < tasks.size(); i++) {
			QueryTask st = tasks.get(i);
			if(st == task)
				return i;
		}
		return -1;
	}
	
	public void addTask(QueryTask t) {
		t.addTaskListener(listener);
		tasks.add(t);
		
		super.fireTableRowsInserted(tasks.size()-1, tasks.size()-1);
	}
	
	private class TaskListener implements PhonTaskListener {

		/* (non-Javadoc)
		 * @see ca.phon.application.PhonTaskListener#propertyChanged(ca.phon.application.PhonTask, java.lang.String, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void propertyChanged(PhonTask task, String property,
				Object oldValue, Object newValue) {
			int taskIndex = getTaskIndex(task);
			fireTableRowsUpdated(taskIndex, taskIndex);
		}

		/* (non-Javadoc)
		 * @see ca.phon.application.PhonTaskListener#statusChanged(ca.phon.application.PhonTask, ca.phon.application.PhonTask.TaskStatus, ca.phon.application.PhonTask.TaskStatus)
		 */
		@Override
		public void statusChanged(PhonTask task, TaskStatus oldStatus,
				TaskStatus newStatus) {
			int taskIndex = getTaskIndex(task);
			fireTableRowsUpdated(taskIndex, taskIndex);
		}
		
	}
}

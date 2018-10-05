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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import ca.phon.query.db.ResultSet;
import ca.phon.query.script.QueryTask;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;

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

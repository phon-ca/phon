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
package ca.phon.app.project.git;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import org.eclipse.jgit.api.Status;


public class CommitTableModel extends AbstractTableModel {
	
	public enum Column {
		STATUS("Status"),
		PATH("Path");
		
		private String name;
		
		private Column(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public enum FileStatus {
		ADDED,
		REMOVED,
		CHANGED,
		MISSING,
		UNTRACKED,
		MODIFIED,
		CONFLICTING;
	}
	
	private boolean indexed = true;
	
	private Status status;
	
	public CommitTableModel(boolean indexed) {
		this.indexed = indexed;
	}
	
	public CommitTableModel(Status status, boolean indexed) {
		super();
		this.indexed = indexed;
		this.status = status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
		super.fireTableDataChanged();
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public List<String> getList() {
		List<String> retVal = new ArrayList<>();
		if(indexed) {
			retVal.addAll(status.getAdded());
			retVal.addAll(status.getRemoved());
			retVal.addAll(status.getChanged());
		} else {
			retVal.addAll(status.getMissing());
			retVal.addAll(status.getUntracked());
			retVal.addAll(status.getModified());
			retVal.addAll(status.getConflicting());
		}
		return retVal;
	}

	@Override
	public int getRowCount() {
		return getList().size();
	}

	@Override
	public int getColumnCount() {
		return Column.values().length;
	}
	
	@Override
	public String getColumnName(int col) {
		return Column.values()[col].getName();
	}
	
	public FileStatus getStatus(String name) {
		FileStatus retVal = null;
		
		if(status.getAdded().contains(name)) {
			retVal = FileStatus.ADDED;
		} else if(status.getRemoved().contains(name)) {
			retVal = FileStatus.REMOVED;
		} else if(status.getChanged().contains(name)) {
			retVal = FileStatus.CHANGED;
		} else if(status.getMissing().contains(name)) {
			retVal = FileStatus.MISSING;
		} else if(status.getModified().contains(name)) {
			retVal = FileStatus.MODIFIED;
		} else if(status.getConflicting().contains(name)) {
			retVal = FileStatus.CONFLICTING;
		} else if(status.getUntracked().contains(name)) {
			retVal = FileStatus.UNTRACKED;
		}
		
		return retVal;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		List<String> list = getList();
		Object retVal = "";
		
		if(rowIndex >= 0 && rowIndex < list.size()) {
			String name = list.get(rowIndex);
			if(columnIndex == 0) {
				// get status
				retVal = getStatus(name);
			} else if(columnIndex == 1) {
				retVal = name;
			}
		}
		
		return retVal;
	}

}

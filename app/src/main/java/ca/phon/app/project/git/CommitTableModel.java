package ca.phon.app.project.git;

import java.util.ArrayList;
import java.util.List;

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

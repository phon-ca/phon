package ca.phon.app.opgraph.nodes.query;

import java.util.ArrayList;
import java.util.List;


public class InventorySettings {

	private ColumnInfo groupBy;
	
	private List<ColumnInfo> columns;
	
	public InventorySettings() {
		super();
		
		columns = new ArrayList<>();
	}
	
	public ColumnInfo getGroupBy() {
		return this.groupBy;
	}
	
	public void setGroupBy(ColumnInfo info) {
		this.groupBy = info;
	}
	
	public List<ColumnInfo> getColumns() {
		return this.columns;
	}
	
	public void removeColumn(ColumnInfo info) {
		this.columns.remove(info);
	}
	
	public void removeColumn(int idx) {
		this.columns.remove(idx);
	}
	
	public ColumnInfo addColumn(String col, boolean caseSensitive, boolean ignoreDiacritics) {
		ColumnInfo info = new ColumnInfo();
		info.name = col;
		info.caseSensitive = caseSensitive;
		info.ignoreDiacritics = ignoreDiacritics;
		addColumn(info);
		return info;
	}
	
	public void addColumn(ColumnInfo info) {
		columns.add(info);
	}
	
	public static class ColumnInfo {
		String name = "";
		boolean caseSensitive = false;
		boolean ignoreDiacritics = true;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isCaseSensitive() {
			return caseSensitive;
		}
		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}
		public boolean isIgnoreDiacritics() {
			return ignoreDiacritics;
		}
		public void setIgnoreDiacritics(boolean ignoreDiacritics) {
			this.ignoreDiacritics = ignoreDiacritics;
		}
		
	}
	
}

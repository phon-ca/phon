/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.nodes.table;

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

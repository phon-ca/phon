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
package ca.phon.script.scripttable;

import java.util.*;

/**
 * A table model that uses a list of object as row data.  The following
 * properties are supplied to each column script:
 * 
 *  * row - integer for the current row
 *  * col - integer for the current column
 *  * rowValue - value of data for row
 * 
 */
public class ListScriptTableModel<T> extends AbstractScriptTableModel {

	private static final long serialVersionUID = 3277560475293409146L;

	private final List<T> rowData;
	
	public ListScriptTableModel(List<T> rowData) {
		super();
		this.rowData = rowData;
	}
	
	@Override
	public int getRowCount() {
		return rowData.size();
	}

	@Override
	public Map<String, Object> getMappingsAt(int row, int col) {
		final Map<String, Object> retVal = super.getMappingsAt(row, col);
		
		retVal.put("row", row);
		retVal.put("col", col);
		retVal.put("rowData", rowData.get(row));
		
		return retVal;
	}
	
	/**
	 * Return row data
	 * 
	 * @return row data
	 */
	public List<T> getRowData() {
		return this.rowData;
	}
}

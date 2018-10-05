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
package ca.phon.script.scripttable;

import java.util.List;
import java.util.Map;

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

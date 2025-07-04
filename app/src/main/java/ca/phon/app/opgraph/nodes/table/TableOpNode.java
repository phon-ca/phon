/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.table;


import ca.phon.opgraph.*;
import ca.phon.query.report.datasource.TableDataSource;

import java.util.*;

public abstract class TableOpNode extends OpNode {
	
	protected InputField tableInput = new InputField("table", "Input table", false,
			true, TableDataSource.class);
	
	protected OutputField tableOutput = new OutputField("table", "Output table",
			true, TableDataSource.class);
	
	public TableOpNode() {
		super();
		
		putField(tableInput);
		putField(tableOutput);
	}
	
	public TableDataSource getInputTable(OpContext context) {
		return (TableDataSource)context.get(tableInput);
	}
	
	public void setTableOutput(OpContext context, TableDataSource tbl) {
		context.put(tableOutput, tbl);
	}
	
	public int getColumnIndex(TableDataSource table, String column) {
		column = column.trim();
		int cIdx = -1;
		for(int j = 0; j < table.getColumnCount(); j++) {
			if(table.getColumnTitle(j).equalsIgnoreCase(column)) {
				cIdx = j;
				break;
			}
		}
		if(cIdx < 0) {
			// attempt to parse as integer
			if(column.matches("[0-9]+")) {
				cIdx = Integer.parseInt(column);
				if(cIdx >= table.getColumnCount())
					cIdx = -1;
			}
		}
		return cIdx;
	}
	
	public int[] getColumnIndices(TableDataSource table, List<String> columns) {
		List<Integer> list = new ArrayList<>();
	
		for(int i = 0; i < columns.size(); i++) {
			int colIdx = getColumnIndex(table, columns.get(i));
			if(colIdx >= 0 && colIdx < table.getColumnCount())
				list.add(colIdx);
		}
		
		int retVal[] = new int[list.size()];
		for(int i = 0 ; i < list.size(); i++) retVal[i] = list.get(i);
		return retVal;
	}
	
}

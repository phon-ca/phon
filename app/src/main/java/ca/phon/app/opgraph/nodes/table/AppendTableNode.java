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
package ca.phon.app.opgraph.nodes.table;

import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
	name="Append Table",
	description="Append rows from another table",
	category="Table",
	showInLibrary=true
)
public class AppendTableNode extends TableOpNode {

	public InputField table1InputField =
			new InputField("table1", "Table 1", false, true, TableDataSource.class);

	public InputField table2InputField =
			new InputField("table2", "Table 2", false, true, TableDataSource.class);

	private boolean preferTable1ColumnNames = true;

	public AppendTableNode() {
		super();

		removeField(tableInput);

		putField(table1InputField);
		putField(table2InputField);
	}

	public boolean isPreferTable1ColumnNames() {
		return this.preferTable1ColumnNames;
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table1 = (DefaultTableDataSource)context.get(table1InputField);
		final DefaultTableDataSource table2 = (DefaultTableDataSource)context.get(table2InputField);
		
		if(table1 != null && table2 != null) {
			final DefaultTableDataSource outputTable = new DefaultTableDataSource();
			// setup column names
			int numCols = Math.max(table1.getColumnCount(), table2.getColumnCount());
			for(int col = 0; col < numCols; col++) {
				String colName =
						(col < table1.getColumnCount() ? table1.getColumnTitle(col) : table2.getColumnTitle(col));
				outputTable.setColumnTitle(col, colName);
			}
	
			outputTable.append(table1);
			outputTable.append(table2);
			
			context.put(tableOutput, outputTable);
		} else if(table1 == null && table2 != null) {
			context.put(tableOutput, table2);
		} else if(table2 == null && table1 != null) {
			context.put(tableOutput, table1);
		} else {
			context.put(tableOutput, new DefaultTableDataSource());
		}
	}

}

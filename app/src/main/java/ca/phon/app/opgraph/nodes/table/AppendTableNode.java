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

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
	name="Append Table",
	description="Append rows from another table",
	category="Report",
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
	}

}

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
package ca.phon.app.opgraph.nodes.query;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mozilla.javascript.Scriptable;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;

@OpNodeInfo(
		name="Partition Table",
		description="Partition table using a boolean expression",
		category="Report",
		showInLibrary=true
)
public class PartitionTableNode extends TableScriptNode {
	
	private final static String DEFAULT_SCRIPT = "function filterRow(table, row) {\n\treturn false;\n}\n";

	private OutputField trueTableOutput = 
			new OutputField("trueTable", "Table of values where the row filter returned true", true, TableDataSource.class);
	
	private OutputField falseTableOutput =
			new OutputField("falseTable", "Table of values where the row filter returned false", true, TableDataSource.class);
	
	public PartitionTableNode() {
		super(DEFAULT_SCRIPT);
		
		removeField(tableOutput);
		
		putField(trueTableOutput);
		putField(falseTableOutput);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
		final DefaultTableDataSource trueTable = new DefaultTableDataSource();
		final DefaultTableDataSource falseTable = new DefaultTableDataSource();
		
		final PhonScript phonScript = getScript();
		final PhonScriptContext scriptContext = phonScript.getContext();
		
		try {
			final Scriptable scope = scriptContext.getEvaluatedScope();
			scriptContext.installParams(scope);
			
			for(int row = 0; row < table.getRowCount(); row++) {
				checkCanceled();
				
				final Map<String, Object> rowData = new LinkedHashMap<>();
				
				for(int col = 0; col < table.getColumnCount(); col++) {
					rowData.put(table.getColumnTitle(col), table.getValueAt(row, col));
				}
				
				Object filterRow = scriptContext.callFunction(scope, "filterRow", 
						context, table, row);
				if(Boolean.parseBoolean(filterRow.toString())) {
					trueTable.addRow(rowData.values().toArray());
				} else {
					falseTable.addRow(rowData.values().toArray());
				}
			}
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e);
		}
		
		for(int col = 0; col < table.getColumnCount(); col++) {
			if(trueTable.getRowCount() > 0)
				trueTable.setColumnTitle(col, table.getColumnTitle(col));
			if(falseTable.getRowCount() > 0)
				falseTable.setColumnTitle(col, table.getColumnTitle(col));
		}
		
		context.put(trueTableOutput, trueTable);
		context.put(falseTableOutput, falseTable);
	}

}

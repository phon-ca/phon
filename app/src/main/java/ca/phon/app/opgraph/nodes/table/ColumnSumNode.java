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

import ca.phon.app.log.LogUtil;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.query.report.datasource.DefaultTableDataSource;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@OpNodeInfo(name="Sum Columns", category="Table", description="Add a row of column sums to the input table", showInLibrary=true)
public class ColumnSumNode extends TableOpNode implements NodeSettings {
	
	private JPanel settingsPanel;
	private JTextArea columnsArea;
	
	private List<String> columns = new ArrayList<>();
	
	public ColumnSumNode() {
		super();
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource inputTable = (DefaultTableDataSource)context.get(tableInput);

		final List<String> columns = getColumns();
		final Map<String, Double> columnSum = sumColumns(inputTable, columns);
		
		var totalsRow = new Object[inputTable.getColumnCount()];
		for(int i = 0; i < totalsRow.length; i++) totalsRow[i] = "";
		for(String colName:columns) {
			int colIdx = inputTable.getColumnIndex(colName);
			if(colIdx >= 0) {
				totalsRow[colIdx] = columnSum.get(colName);
			}
		}
		inputTable.addRow(totalsRow);
		
		context.put(tableOutput, inputTable);
	}
	
	public Map<String, Double> sumColumns(DefaultTableDataSource table, List<String> columns) {
		final Map<String, Double> columnSum = new HashMap<>();
		final Map<String, Integer> columnIndices = new HashMap<>();
		columns.forEach( (colName) -> columnSum.put(colName, 0.0) );
		columns.forEach( (colName) -> columnIndices.put(colName, table.getColumnIndex(colName)) );
		for(int row = 0; row < table.getRowCount(); row++) {
			var rowData = table.getRow(row);
			for(String colName:columns) {
				var sum = columnSum.get(colName);
				int colIdx = columnIndices.get(colName);
				
				if(colIdx >= 0) {
					var rowVal = rowData[colIdx];
					if(rowVal instanceof Number) {
						sum += ((Number) rowVal).doubleValue();
					}
					columnSum.put(colName, sum);
				}
			}
		}
		return columnSum;
	}
	
	public List<String> getColumns() {
		return (this.columnsArea != null ? parseColumns(columnsArea.getText()) : this.columns);
	}

	public void setColumns(String columnTxt) {
		this.columns = parseColumns(columnTxt);
		if(this.columnsArea != null) {
			this.columnsArea.setText(columnTxt);
		}
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
		if(this.columnsArea != null) {
			this.columnsArea.setText(
					this.columns.stream().collect(Collectors.joining("\n")) );
		}
	}

	private List<String> parseColumns(String txt) {
		List<String> columns = new ArrayList<>();
		try ( BufferedReader reader = new BufferedReader(new StringReader(txt)) ) {
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				columns.add(line);
			}
		} catch (IOException e) {
			LogUtil.warning(e);
		}
		return columns;
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			
			String columnsTxt = getColumns().stream().collect(Collectors.joining("\n"));
			columnsArea = new JTextArea();
			columnsArea.setText(columnsTxt);
			final JScrollPane scroller = new JScrollPane(columnsArea);
			
			settingsPanel.add(scroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		retVal.setProperty("columns", getColumns().stream().collect(Collectors.joining("\n")) );
		return retVal;
	}

	@Override
	public void loadSettings(Properties properties) {
		setColumns(properties.getProperty("columns", ""));
	}

}

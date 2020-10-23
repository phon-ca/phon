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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.query.report.datasource.*;

@OpNodeInfo(category = "Table", description = "Create a new table with specified columns", name = "Create Table", showInLibrary = true)
public class CreateTableNode extends OpNode implements NodeSettings {
	
	private final static InputField columnNames = new InputField("columns", "Column names in an array of strings", true, true, String[].class, Collection.class);

	private final static OutputField tableOutput = new OutputField("table", "Output table", true, TableDataSource.class);
	
	private JPanel settingsPanel;
	
	private JTextArea columnArea;
	
	private String[] columns = new String[0];
	
	public  CreateTableNode() {
		super();
		
		putField(columnNames);
		putField(tableOutput);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		List<String> colList = new ArrayList<>();
		
		Object columnNamesObj = context.get(columnNames);
		if(columnNamesObj != null) {
			if(columnNamesObj instanceof String[]) {
				String[] a = (String[])columnNamesObj;
				colList.addAll(List.of(a));
			} else if(columnNamesObj instanceof Collection) {
				Collection<?> c = (Collection<?>)columnNamesObj;
				c.forEach( (col) -> colList.add(col.toString()) );
			}
		} else {
			colList.addAll(List.of(getColumns()));
		}
		
		if(colList.size() == 0) {
			throw new ProcessingException(null, "Output table has no columns");
		}
		
		DefaultTableDataSource table = new DefaultTableDataSource();
		int colIdx = 0;
		for(String columnName:colList) {
			table.setColumnTitle(colIdx++, columnName);
		}
		
		context.put(tableOutput, table);
	}
	
	public String[] getColumns() {
		if(columnArea != null) {
			List<String> cols = new ArrayList<>(); 
			String colText = columnArea.getText();
			try (BufferedReader reader = new BufferedReader(new StringReader(colText))) {
				String line = null;
				while((line = reader.readLine()) != null) {
					if(line.trim().length() > 0) {
						cols.add(line.trim());
					}
				}
			} catch (IOException e) {
				LogUtil.warning(e);
			}
			return cols.toArray(new String[0]);
		} else {
			return columns;
		}
	}
	
	public void setColumns(String[] cols) {
		this.columns = Arrays.copyOf(cols, cols.length);
		if(this.columnArea != null) {
			String colText = Arrays.stream(cols).collect(Collectors.joining("\n"));
			this.columnArea.setText(colText);
		}
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			columnArea = new JTextArea();
			JScrollPane scroller = new JScrollPane(columnArea);
			
			String colText = Arrays.stream(this.columns).collect(Collectors.joining("\n"));
			this.columnArea.setText(colText);
			
			settingsPanel.add(scroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties props = new Properties();
		
		String colText = Arrays.stream(getColumns()).collect(Collectors.joining(","));
		props.put("columns", colText);
		
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		String colText = properties.getProperty("columns");
		if(colText.trim().length() > 0) {
			String[] cols = colText.trim().split(",");
			setColumns(cols);
		}
	}
	
}

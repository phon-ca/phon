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
package ca.phon.app.log;

import java.io.*;

import au.com.bytecode.opencsv.*;

/**
 * Convert buffer table to an HTML table.
 * 
 */
public class HTMLTableBufferExporter implements BufferExporter<String> {
	
	private boolean useFirstRowAsColumnHeader = true;
	
	public HTMLTableBufferExporter() {
		super();
	}
	
	public HTMLTableBufferExporter(boolean useFirstRowAsColumnHeader) {
		super();
		this.useFirstRowAsColumnHeader = useFirstRowAsColumnHeader;
	}

	@Override
	public String exportBuffer(LogBuffer logBuffer) throws BufferExportException {
		final CSVReader reader = new CSVReader(new StringReader(logBuffer.getText()));
		final CSVTableModel tableModel = new CSVTableModel(reader);
		tableModel.setUseFirstRowAsHeader(this.useFirstRowAsColumnHeader);
		
		// TODO Add styles
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append("<table style='report-table'>\n");
		buffer.append("\t<tr style='report-table-header-row'>\n");
		for(int col = 0; col < tableModel.getColumnCount(); col++) {
			String colName = tableModel.getColumnName(col);
			buffer.append("\t\t<th style='report-table-header'>").append(colName).append("</th>\n");
		}
		buffer.append("\t</tr>\n");
		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			buffer.append("\t<tr style='report-table-row").append(row % 2).append("'>\n");
			
			for(int col = 0; col < tableModel.getColumnCount(); col++) {
				buffer.append("\t\t<td style='report-table-cell'>");
				buffer.append(tableModel.getValueAt(row, col));
				buffer.append("</td>\n");
			}
			
			buffer.append("\t</tr>\n");
		}
		buffer.append("\t<tr style='report-table-header-row'>\n");
		
		return buffer.toString();
	}

}

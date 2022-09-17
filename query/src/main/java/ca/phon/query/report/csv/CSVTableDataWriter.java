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
package ca.phon.query.report.csv;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.util.OSInfo;

import javax.swing.*;
import java.io.*;

/**
 * Writes a csv file based on the values displayed
 * in a given {@link JTable}.  Note, this only outputs
 * the displayed values, not the entire table model.
 *
 */
public class CSVTableDataWriter {

	/**
	 * CSV separator char
	 */
	public static final String CSV_SEP_CHAR = "\t";
	
	/**
	 * CSV quote char
	 */
	public static final String CSV_QUOTE_CHAR = "\"";
	
	/**
	 * CSV line term
	 */
	public static final String CSV_LINE_TERM = System.getProperty("line.separator");
	
	/**
	 * File Encoding
	 */
	public static final String DEFAULT_FILE_ENCODING = "UTF-8";
	
	private String charEncoding = DEFAULT_FILE_ENCODING;
	
	public CSVTableDataWriter() {
		this(DEFAULT_FILE_ENCODING);
	}
	
	public CSVTableDataWriter(String encoding) {
		this.charEncoding = encoding;
	}
	
	public void writeTableToFile(JTable table, File file) 
		throws IOException {
		final CSVWriter writer = 
				new CSVWriter(new PrintWriter(file, charEncoding), ',', '\"', 
						(OSInfo.isWindows() ? "\r\n" : "\n"));
		// output column names
		final String[] colnames = new String[table.getColumnCount()];
		for(int i = 0; i < table.getColumnCount(); i++) {
			colnames[i] = table.getColumnName(i);
		}
		writer.writeNext(colnames);
		
		final String[] currentRow = new String[table.getColumnCount()];
		for(int row = 0; row < table.getRowCount(); row++) {
			for(int col = 0; col < table.getColumnCount(); col++) {
				final Object cellVal = table.getValueAt(row, col);
				currentRow[col] = (cellVal == null ? "" : cellVal.toString());
			}
			writer.writeNext(currentRow);
		}
		writer.flush();
		writer.close();
	}
	
}

/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.report.csv;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JTable;

import ca.phon.util.OSInfo;

import au.com.bytecode.opencsv.CSVWriter;

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
	public static final String FILE_ENCODING = "UTF-8";
	
	public CSVTableDataWriter() {
		super();
	}
	
	public void writeTableToFile(JTable table, File file) 
		throws IOException {
		final CSVWriter writer = 
				new CSVWriter(new PrintWriter(file), ',', '\"', 
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

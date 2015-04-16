/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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

import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.query.report.datasource.TableDataSource;
/**
 * Write the data for the tabular data sources
 * to the given csv writer.
 *
 */
public class CSVTableDataSourceWriter implements CSVSectionWriter {
	
	/**
	 * Data source
	 */
	private TableDataSource dataSource;
	
	/**
	 * Reference to report builder.  Use to detect when
	 * report generation has been canceled.
	 */
	private CSVReportBuilder reportBuilder;
	
	public CSVTableDataSourceWriter(CSVReportBuilder builder, TableDataSource ds) {
		this.reportBuilder = builder;
		this.dataSource = ds;
	}
	
	public TableDataSource getDataSource() {
		return this.dataSource;
	}

	@Override
	public void writeSection(CSVWriter writer, int indentLevel) {
		List<String> currentLine = new ArrayList<String>();
		for(int i = 0; i < indentLevel; i++) currentLine.add(new String());
		
		// writer header line
		for(int col = 0; col < dataSource.getColumnCount(); col++) {
			currentLine.add(dataSource.getColumnTitle(col));
		}
		writer.writeNext(currentLine.toArray(new String[0]));
		
		for(int row = 0; row < dataSource.getRowCount(); row++) {
			
			// get out if report generation is canceled.
			if(reportBuilder.isBuildCanceled()) break;
			
			currentLine.clear();
			for(int i = 0; i < indentLevel; i++) currentLine.add(new String());
			for(int col = 0; col < dataSource.getColumnCount(); col++) {
				Object val = dataSource.getValueAt(row, col);
				String txt = (val == null ? "" : val.toString());
				currentLine.add(txt);
			}
			writer.writeNext(currentLine.toArray(new String[0]));
		}
	}

}

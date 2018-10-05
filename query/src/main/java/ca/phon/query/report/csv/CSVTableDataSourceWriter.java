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

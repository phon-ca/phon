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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.query.report.datasource.ResultListingDataSource;
import ca.phon.query.report.io.ResultListingFormatType;

public class CSVResultListingDataSourceWriter extends CSVTableDataSourceWriter {

	private static final Logger LOGGER = Logger
			.getLogger(CSVResultListingDataSourceWriter.class.getName());
	
	public CSVResultListingDataSourceWriter(CSVReportBuilder builder, ResultListingDataSource ds) {
		super(builder, ds);
	}

	@Override
	public void writeSection(CSVWriter writer, int indentLevel) {
		ResultListingDataSource rsDs = (ResultListingDataSource)super.getDataSource();
		if(rsDs.getFormat() == ResultListingFormatType.TABLE) {
			super.writeSection(writer, indentLevel);
		} else {
			List<String> currentLine = new ArrayList<String>();
			for(int i = 0; i < indentLevel; i++) currentLine.add(new String());
			
			// writer header line
			for(int col = 0; col < rsDs.getColumnCount(); col++) {
				currentLine.add(rsDs.getColumnTitle(col));
			}
			writer.writeNext(currentLine.toArray(new String[0]));
			
			for(int row = 0; row < rsDs.getRowCount(); row++) {
				currentLine.clear();
				for(int i = 0; i < indentLevel; i++) currentLine.add(new String());
				for(int col = 0; col < rsDs.getColumnCount(); col++) {
					Object val = rsDs.getValueAt(row, col);
					String txt = (val == null ? "" : val.toString());
					
					// if last column - expand groups
					if(txt.startsWith("[") && txt.endsWith("]") && col == rsDs.getColumnCount()-1) {
						final Pattern grpPattern = Pattern.compile("\\[([^\\[\\]])*)\\]");
						final Matcher grpMatcher = grpPattern.matcher(txt);
						int currentIdx = 0;
						while(grpMatcher.find(currentIdx)) {
								currentLine.add("[" + grpMatcher.group(1) + "]");
								currentIdx = grpMatcher.end();
						}
					} else {
						currentLine.add(txt);
					}
				}
				writer.writeNext(currentLine.toArray(new String[0]));
			}
		}
	}
	
}

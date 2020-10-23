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

import java.util.*;
import java.util.regex.*;

import org.apache.logging.log4j.*;

import au.com.bytecode.opencsv.*;
import ca.phon.query.report.datasource.*;
import ca.phon.query.report.io.*;

public class CSVResultListingDataSourceWriter extends CSVTableDataSourceWriter {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(CSVResultListingDataSourceWriter.class.getName());
	
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

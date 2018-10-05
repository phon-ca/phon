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
package ca.phon.query.report.datasource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import au.com.bytecode.opencsv.CSVWriter;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterType;
import ca.phon.formatter.FormatterUtil;
import ca.phon.ipa.IPATranscript;
import ca.phon.util.PhonConstants;

@FormatterType(value=TableDataSource.class)
public class TableDataSourceFormatter implements Formatter<TableDataSource> {

	// used only when parsing
	private boolean useFirstColumnAsHeader = true;

	public TableDataSourceFormatter() {
		super();
	}

	public TableDataSourceFormatter(boolean useFirstColumnAsHeader) {
		super();

		this.useFirstColumnAsHeader = useFirstColumnAsHeader;
	}

	@Override
	public String format(TableDataSource table) {
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		final PrintWriter writer = new PrintWriter(bout);

		try (final CSVWriter csvWriter = new CSVWriter(writer)) {
			String[] row = new String[table.getColumnCount()];
			for(int i = 0; i < table.getColumnCount(); i++)
				row[i] = table.getColumnTitle(i);
			csvWriter.writeNext(row);

			for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
				for(int i = 0; i < table.getColumnCount(); i++) {
					Object val = table.getValueAt(rowIdx, i);

					if(val == null) {
						row[i] = "";
					} else {
						// insert 'null' if we have an empty IPA transcript
						if(val instanceof IPATranscript && ((IPATranscript)val).length() == 0) {
							row[i] = PhonConstants.nullChar + "";
						} else {
							row[i] = FormatterUtil.format(val);
						}
					}
				}
				csvWriter.writeNext(row);
			}
		} catch (IOException e) {

		}

		String retVal = new String();
		try {
			retVal = bout.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {}
		return retVal;
	}

	@Override
	public TableDataSource parse(String text) throws ParseException {
		throw new ParseException(text, -1);
	}

}
